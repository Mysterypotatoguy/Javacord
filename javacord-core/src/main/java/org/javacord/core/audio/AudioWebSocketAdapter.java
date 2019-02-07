package org.javacord.core.audio;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.neovisionaries.ws.client.*;
import org.apache.logging.log4j.Logger;
import org.javacord.api.audio.AudioConnection.VoiceConnectionStatus;
import org.javacord.api.audio.SpeakingFlag;
import org.javacord.core.DiscordApiImpl;
import org.javacord.core.util.concurrent.ThreadFactory;
import org.javacord.core.util.gateway.VoiceGatewayOpcode;
import org.javacord.core.util.gateway.WebSocketCloseCode;
import org.javacord.core.util.logging.LoggerUtil;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class AudioWebSocketAdapter extends WebSocketAdapter {

    private static final Logger logger = LoggerUtil.getLogger(AudioWebSocketAdapter.class);

    private static final int VOICE_GATEWAY_VERSION = 4;

    private WebSocket webSocket;

    private int heartbeatInterval;
    private ScheduledFuture<?> heartbeatFuture;

    private String sessionId;
    private String endpoint;
    private String token;

    private DiscordApiImpl api;
    private AudioConnectionImpl voiceConnection;

    private boolean shouldReconnect = true;
    private boolean isReconnecting = false;

    /**
     * Constructs a new AudioWebSocketAdapter instance.
     *
     * @param api             The DiscordApi instance.
     * @param voiceConnection The AudioConnection to attach to.
     * @param endpoint        The endpoint to connect to.
     * @param token           The voice token received.
     */
    public AudioWebSocketAdapter(DiscordApiImpl api, AudioConnectionImpl voiceConnection, String endpoint,
                                 String token) {
        this.api = api;
        this.voiceConnection = voiceConnection;
        this.sessionId = api.getWebSocketAdapter().getSessionId();
        this.endpoint = "wss://" + endpoint.replace(":80", "") + "/?v=" + VOICE_GATEWAY_VERSION;
        this.token = token;
    }

    /**
     * Connects the websocket to the voice server.
     */
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

    /**
     * Reconnects the websocket to the voice server.
     */
    public void reconnect() {
        if (voiceConnection.getConnectionStatus() != VoiceConnectionStatus.DISCONNECTED) {
            disconnect();
        }
        logger.debug("Attempting a reconnect to voice channel " + voiceConnection.getConnectedChannel().getId());
        //TODO: Backoff?
        connect();
    }

    /**
     * Disconnects the websocket from the voice server.
     */
    public void disconnect() {
        shouldReconnect = false;
        heartbeatFuture.cancel(true);
        webSocket.disconnect(WebSocketCloseCode.NORMAL.getCode(), "Disconnecting");
    }

    @Override
    public void onTextMessage(WebSocket websocket, String message) throws Exception {
        ObjectMapper objectMapper = api.getObjectMapper();
        JsonNode packet = objectMapper.readTree(message);
        JsonNode data = packet.get("d");
        int op = packet.get("op").asInt();
        Optional<VoiceGatewayOpcode> opcodeOptional = VoiceGatewayOpcode.fromCode(op);
        if (!opcodeOptional.isPresent()) {
            logger.debug("Received an unknown packet! (content: {})", packet.toString());
            return;
        }
        VoiceGatewayOpcode opcode = opcodeOptional.get();
        logger.debug("Received packet! (op: {}, content: {})", opcode, packet.toString());
        switch (opcode) {
            case READY:
                InetSocketAddress udpAddress = new InetSocketAddress(data.get("ip").asText(), data.get("port").asInt());
                int ssrc = data.get("ssrc").asInt();
                voiceConnection.setUdpSocket(new AudioUdpSocket(voiceConnection, udpAddress, ssrc));
                sendSelectProtocol(voiceConnection.getUdpSocket().findLocalUdpAddress());
                break;
            case SESSION_DESCRIPTION:
                byte[] secretKey = objectMapper.convertValue(data.get("secret_key"), byte[].class);
                voiceConnection.getUdpSocket().setSecretKey(secretKey);
                voiceConnection.getUdpSocket().startSendingThread();
                voiceConnection.setConnectionStatus(VoiceConnectionStatus.CONNECTED);
                voiceConnection.getConnectedFuture().complete(voiceConnection);
                break;
            case SPEAKING:
                // TODO: Handle SPEAKING
            case HEARTBEAT_ACK:
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
                //This should be impossible
                break;
        }
    }

    /**
     * Sends an OP 0 IDENTIFY packet via voice websocket.
     */
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

    /**
     * Sends an OP 1 SELECT packet via voice websocket.
     */
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

    /**
     * Sends an OP 5 SPEAKING packet via voice websocket.
     */
    public void sendSpeakingUpdate() {
        EnumSet<SpeakingFlag> speakingFlags = voiceConnection.getSpeakingFlags();
        int speakingMode = 0;
        for (SpeakingFlag flag : speakingFlags) {
            speakingMode |= flag.getFlag();
        }
        ObjectNode packet = api.getObjectMapper().createObjectNode();
        packet.put("op", 5)
                .putObject("d")
                .put("speaking", speakingMode)
                .put("delay", 0)
                .put("ssrc", voiceConnection.getUdpSocket().getSsrc());
        webSocket.sendText(packet.toString());
        logger.debug("Sent SPEAKING " + packet.toString());
    }

    /**
     * Sends an OP 7 RESUME packet via voice websocket.
     */
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

    /**
     * Starts an executor to send an OP 3 HEARTBEAT at the set interval given.
     */
    private void startHeartbeat() {
        ScheduledExecutorService heartbeatExecutorService =
                Executors.newSingleThreadScheduledExecutor(new ThreadFactory(
                "Javacord - Audio Heartbeat Thread (Guild:" + voiceConnection.getServer().getId() + ")",
                true));
        heartbeatFuture = heartbeatExecutorService.scheduleAtFixedRate(() -> {
            ObjectNode packet = api.getObjectMapper().createObjectNode();
            packet.put("op", 3).put("d", System.currentTimeMillis());
            webSocket.sendText(packet.toString());
            logger.debug("Sent heartbeat (Interval: " + heartbeatInterval + ")");
        }, heartbeatInterval, heartbeatInterval, TimeUnit.MILLISECONDS);
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
        heartbeatFuture.cancel(true);
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