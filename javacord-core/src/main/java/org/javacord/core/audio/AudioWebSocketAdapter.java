package org.javacord.core.audio;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.neovisionaries.ws.client.ThreadType;
import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketAdapter;
import com.neovisionaries.ws.client.WebSocketException;
import com.neovisionaries.ws.client.WebSocketFactory;
import com.neovisionaries.ws.client.WebSocketFrame;
import org.apache.logging.log4j.Logger;
import org.javacord.api.audio.VoiceConnection.VoiceConnectionStatus;
import org.javacord.core.DiscordApiImpl;
import org.javacord.core.util.concurrent.ThreadFactory;
import org.javacord.core.util.gateway.VoiceGatewayOpcode;
import org.javacord.core.util.gateway.WebSocketCloseCode;
import org.javacord.core.util.logging.LoggerUtil;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class AudioWebSocketAdapter extends WebSocketAdapter {

    private static final Logger logger = LoggerUtil.getLogger(AudioWebSocketAdapter.class);

    private static final int VOICE_GATEWAY_VERSION = 4;

    private WebSocket webSocket;

    private int heartbeatInterval;
    private ScheduledExecutorService heartbeatExecutorService;

    private String sessionId;
    private String endpoint;
    private String token;

    private DiscordApiImpl api;
    private ImplVoiceConnection voiceConnection;

    private boolean shouldReconnect = true;
    private boolean isReconnecting = false;

    public AudioWebSocketAdapter(DiscordApiImpl api, ImplVoiceConnection voiceConnection, String endpoint,
                                 String token) {
        this.api = api;
        this.voiceConnection = voiceConnection;
        this.sessionId = api.getWebSocketAdapter().getSessionId();
        this.endpoint = "wss://" + endpoint.replace(":80", "") + "/?v=" + VOICE_GATEWAY_VERSION;
        this.token = token;
        heartbeatExecutorService = Executors.newSingleThreadScheduledExecutor(new ThreadFactory(
                "Javacord - Audio Heartbeat Thread (Guild:" + voiceConnection.getServer().getId() + ")",
                true));
    }

    public void connect() {
        WebSocketFactory factory = new WebSocketFactory();
        try {
            webSocket = factory.createSocket(endpoint).addListener(this).connectAsynchronously();
            voiceConnection.setConnectionStatus(VoiceConnectionStatus.CONNECTING);
        } catch (IOException e) {
            e.printStackTrace();
            reconnect();
        }
    }

    public void reconnect() {
        if (voiceConnection.getConnectionStatus() != VoiceConnectionStatus.DISCONNECTED) {
            disconnect();
        }
        logger.debug("Attempting a reconnect to voice channel " + voiceConnection.getConnectedChannel().getId());
        connect();
    }

    public void disconnect() {
        shouldReconnect = false;
        heartbeatExecutorService.shutdownNow();
        webSocket.disconnect(WebSocketCloseCode.NORMAL.getCode(), "Disconnecting");
    }

    @Override
    public void onTextMessage(WebSocket websocket, String message) throws Exception {
        ObjectMapper objectMapper = api.getObjectMapper();
        JsonNode packet = objectMapper.readTree(message);
        int op = packet.get("op").asInt();
        Optional<VoiceGatewayOpcode> opcode = VoiceGatewayOpcode.fromCode(op);
        if (!opcode.isPresent()) {
            logger.debug("Received an unknown packet! (op: {}, content: {})", op, packet.toString());
            return;
        }
        logger.debug("Received packet! (op: {}, content: {})", op, packet.toString());
        JsonNode data = packet.get("d");
        switch (opcode.get()) {
            case READY:
                InetSocketAddress udpAddress = new InetSocketAddress(data.get("ip").asText(), data.get("port").asInt());
                int ssrc = data.get("ssrc").asInt();
                voiceConnection.setUdpSocket(new AudioUdpSocket(voiceConnection, udpAddress, ssrc));
                sendSelectProtocol(voiceConnection.getUdpSocket().findLocalUdpAddress());
                break;
            case SESSION_DESCRIPTION:
                byte[] secretKey = objectMapper.convertValue(data.get("secret_key"), byte[].class);
                voiceConnection.getUdpSocket().setSecretKey(secretKey);
                voiceConnection.setConnectionStatus(VoiceConnectionStatus.CONNECTED);
                voiceConnection.getUdpSocket().startSendingThread();
                ((AudioManagerImpl) api.getAudioManager())
                        .completeConnection(voiceConnection.getConnectedChannel().getId(), voiceConnection);
                break;
            case SPEAKING:
                // TODO: Handle SPEAKING
            case HEARTBEAT_ACK:
                logger.debug("Got heartbeat ACK");
                break;
            case HELLO:
                heartbeatInterval = data.get("heartbeat_interval").asInt();
                sendIdentify();
                startHeartbeat();
                break;
            case RESUMED:
                voiceConnection.setConnectionStatus(VoiceConnectionStatus.CONNECTED);
                isReconnecting = false;
                break;
            case CLIENT_DISCONNECT:
                // TODO: Handle CLIENT_DISCONNECT
                break;
            default:
                logger.info("Received a packet that we shouldn't have received! (op: {}, content: {})", op, packet.toString());
                break;
        }
    }

    private void sendIdentify() {
        ObjectNode packet = api.getObjectMapper().createObjectNode();
        packet.put("op", 0)
                .putObject("d")
                .put("server_id", String.valueOf(voiceConnection.getConnectedChannel().getServer().getId()))
                .put("user_id", String.valueOf(api.getClientId()))
                .put("session_id", sessionId)
                .put("token", token);
        webSocket.sendText(packet.toString());
        logger.debug("Sent IDENTIFY " + packet.toString());
    }

    private void sendSelectProtocol(InetSocketAddress address) {
        ObjectNode packet = api.getObjectMapper().createObjectNode();
        packet.put("op", 1)
                .putObject("d")
                .put("protocol", "udp")
                .putObject("data")
                .put("address", address.getHostString())
                .put("port", address.getPort())
                .put("mode", "xsalsa20_poly1305");
        webSocket.sendText(packet.toString());
        logger.debug("Sent SELECT " + packet.toString());
    }

    public void sendSpeakingUpdate(boolean speaking) {
        ObjectNode packet = api.getObjectMapper().createObjectNode();
        packet.put("op", 5)
                .putObject("d")
                .put("speaking", speaking ? 1 : 0)
                .put("delay", 0)
                .put("ssrc", voiceConnection.getUdpSocket().getSsrc());
        webSocket.sendText(packet.toString());
        logger.debug("Sent SPEAKING " + packet.toString());
    }

    private void sendResume() {
        ObjectNode packet = api.getObjectMapper().createObjectNode();
        packet.put("op", 7)
                .putObject("d")
                .put("server_id", String.valueOf(voiceConnection.getConnectedChannel().getServer().getId()))
                .put("session_id", sessionId)
                .put("token", api.getToken());
        webSocket.sendText(packet.toString());
        logger.debug("Sent RESUME " + packet.toString());

    }

    private void startHeartbeat() {
        heartbeatExecutorService.scheduleAtFixedRate(() -> {
            ObjectNode packet = api.getObjectMapper().createObjectNode();
            packet.put("op", 3).put("d", System.currentTimeMillis());
            webSocket.sendText(packet.toString());
            logger.debug("Sent heartbeat (Interval: " + heartbeatInterval + ")");
        }, 0, heartbeatInterval, TimeUnit.MILLISECONDS);
    }

    @Override
    public void onThreadCreated(WebSocket websocket, ThreadType threadType, Thread thread) {
        switch (threadType) {
            case READING_THREAD:
                thread.setName("AudioWebSocketReadingThread - " + voiceConnection.getServer().getId());
                break;
            case CONNECT_THREAD:
                thread.setName("AudioWebSocketConnectThread - " + voiceConnection.getServer().getId());
                break;
            case FINISH_THREAD:
                thread.setName("AudioWebSocketFinishThread - " + voiceConnection.getServer().getId());
                break;
            case WRITING_THREAD:
                thread.setName("AudioWebSocketWritingThread - " + voiceConnection.getServer().getId());
                break;
        }
    }

    @Override
    public void onConnected(WebSocket websocket, Map<String, List<String>> headers) {
        if (isReconnecting) {
            sendResume();
        }
    }

    @Override
    public void onDisconnected(WebSocket websocket, WebSocketFrame serverCloseFrame,
                               WebSocketFrame clientCloseFrame, boolean closedByServer) {
        voiceConnection.setConnectionStatus(VoiceConnectionStatus.DISCONNECTED);
        WebSocketFrame closeFrame = Optional.ofNullable(serverCloseFrame).orElse(clientCloseFrame);
        String closeCode = String.valueOf(closeFrame.getCloseCode());
        String closeReason = Optional.ofNullable(closeFrame.getCloseReason()).orElse("Unknown");
        logger.info("Audio Websocket closed with code {} and reason '{}' by {}",
                closeCode, closeReason, closedByServer ? "server" : "client");
        if (shouldReconnect) {
            //TODO
            if (closedByServer) {
                switch (closeCode) {
                    case "4004": //Authentication failed
                    case "4011": //Server not found
                    case "4014": //Disconnected
                        //Check if this was caused by the channel being deleted
                        if (!voiceConnection.getConnectedChannel().getLatestInstance().isCompletedExceptionally()) {
                            reconnect();
                            return;
                        }
                    case "4015": //Voice server crashed
                        logger.info("Unable to reconnect");
                        shouldReconnect = false;
                        disconnect();
                        break;
                    default:
                        reconnect();
                        return;
                }
            }
            if (!closeCode.equals("1000")) {
                reconnect();
            }
        }
    }

    @Override
    public void onError(WebSocket websocket, WebSocketException cause) {
        // TODO: Rewrite error handling
        cause.printStackTrace();
    }

    @Override
    public void handleCallbackError(WebSocket websocket, Throwable cause) {
        logger.error("Websocket callback error!", cause);
    }

    @Override
    public void onUnexpectedError(WebSocket websocket, WebSocketException cause) {
        logger.warn("Websocket onUnexpected error!", cause);
    }

    @Override
    public void onConnectError(WebSocket websocket, WebSocketException exception) {
        logger.warn("Websocket onConnect error!", exception);
    }

}