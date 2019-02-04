package org.javacord.core.audio;

import org.javacord.api.audio.AudioConnection;
import org.javacord.api.audio.SpeakingFlag;
import org.javacord.api.audio.source.AudioSource;
import org.javacord.api.entity.channel.ServerVoiceChannel;
import org.javacord.api.entity.server.Server;
import org.javacord.api.listener.server.voice.VoiceServerUpdateListener;
import org.javacord.api.util.event.ListenerManager;
import org.javacord.core.DiscordApiImpl;
import org.javacord.core.entity.server.ServerImpl;

import java.util.EnumSet;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

public class AudioConnectionImpl implements AudioConnection {

    private DiscordApiImpl api;
    private CompletableFuture<AudioConnection> connectedFuture;
    private ServerVoiceChannel connectedChannel;

    private boolean selfMuted = false;
    private boolean selfDeafened = false;

    private EnumSet<SpeakingFlag> speakingFlags = EnumSet.of(SpeakingFlag.NONE);

    private VoiceConnectionStatus connectionStatus = VoiceConnectionStatus.CONNECTING;

    private AudioWebSocketAdapter webSocket;
    private AudioUdpSocket udpSocket;

    private AudioSource audioSource;
    // private List<> outputs;

    /**
     * Constructs a new AudioConnectionImpl instance.
     *
     * @param api      The DiscordApi instance
     * @param channel  The channel this connection is connected to
     * @param endpoint The endpoint to connect the websocket to.
     * @param token    The voice token to provide when opening the websocket.
     */
    public AudioConnectionImpl(DiscordApiImpl api, ServerVoiceChannel channel, String endpoint, String token) {
        this.api = api;
        this.connectedChannel = channel;
        webSocket = new AudioWebSocketAdapter(api, this, endpoint, token);
        webSocket.connect();
    }

    public AudioConnectionImpl(DiscordApiImpl api, ServerVoiceChannel channel, boolean selfMute, boolean selfDeafen,
                               CompletableFuture<AudioConnection> future) {
        this.api = api;
        this.connectedFuture = future;
        this.connectedChannel = channel;
        //TODO: Handle logic for duplicate connections
        future.thenAccept(connection -> ((ServerImpl) connection.getServer()).setAudioConnection(this));
        AtomicReference<ListenerManager<VoiceServerUpdateListener>> lm = new AtomicReference<>();
        lm.set(api.addListener(VoiceServerUpdateListener.class, event -> {
            if (event.getServer() != channel.getServer()) {
                return;
            }
            String endpoint = event.getEndpoint();
            String token = event.getToken();
            webSocket = new AudioWebSocketAdapter(api, this, endpoint, token);
            webSocket.connect();
            lm.get().remove();
        }));
        api.getWebSocketAdapter().sendVoiceStateUpdate(
                channel.getServer(),
                channel,
                selfMute,
                selfDeafen);
        //TODO: Add to DiscordApiImpl map
    }

    /**
     * Disconnects the connection and reconnects it to the given channel.
     *
     * @param channel  The channel to connect to.
     * @param endpoint The endpoint to connect to.
     * @param token    The voice token.
     */
    public void reconnect(ServerVoiceChannel channel, String endpoint, String token) {
        disconnect();
        webSocket = new AudioWebSocketAdapter(api, this, endpoint, token);
        webSocket.connect();
        setConnectedChannel(channel);
    }

    @Override
    public CompletableFuture<AudioConnection> moveTo(ServerVoiceChannel voiceChannel) {
        return moveTo(voiceChannel, selfMuted, selfDeafened);
    }

    @Override
    public CompletableFuture<AudioConnection> moveTo(ServerVoiceChannel voiceChannel,
                                                     boolean selfMute,
                                                     boolean selfDeafen) {
        if (connectionStatus == VoiceConnectionStatus.DISCONNECTED) {
            CompletableFuture<AudioConnection> future = new CompletableFuture<>();
            future.completeExceptionally(new IllegalStateException("AudioConnection is disconnected!"));
            return future;
        }
        setSelfMuted(selfMute);
        setSelfDeafened(selfDeafen);
        return ((AudioManagerImpl) api.getAudioManager()).moveConnection(this, voiceChannel);
    }

    @Override
    public CompletableFuture<Void> disconnect() {
        if (connectionStatus == VoiceConnectionStatus.DISCONNECTED) {
            CompletableFuture<Void> future = new CompletableFuture<>();
            future.completeExceptionally(new IllegalStateException("AudioConnection is disconnected!"));
            return future;
        }
        webSocket.disconnect();
        udpSocket.disconnect();
        api.getWebSocketAdapter().sendVoiceStateUpdate(getServer(), null, false, false);
        ((AudioManagerImpl) api.getAudioManager()).removeConnection(connectedChannel.getServer().getId());
        setConnectionStatus(VoiceConnectionStatus.DISCONNECTED);
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public boolean isSelfMuted() {
        return selfMuted;
    }

    @Override
    public void setSelfMuted(boolean muted) {
        selfMuted = muted;
    }

    @Override
    public boolean isSelfDeafened() {
        return selfDeafened;
    }

    @Override
    public void setSelfDeafened(boolean deafened) {
        selfDeafened = deafened;
    }

    @Override
    public boolean isPrioritySpeaking() {
        return speakingFlags.contains(SpeakingFlag.PRIORITY_SPEAKER);
    }

    @Override
    public void setPrioritySpeaking(boolean prioritySpeaking) {
        EnumSet<SpeakingFlag> speakingFlags = getSpeakingFlags();
        if (prioritySpeaking) {
            speakingFlags.add(SpeakingFlag.SPEAKING);
        } else {
            speakingFlags.remove(SpeakingFlag.SPEAKING);
        }
        setSpeakingFlags(speakingFlags);
    }

    @Override
    public EnumSet<SpeakingFlag> getSpeakingFlags() {
        return EnumSet.copyOf(speakingFlags);
    }

    /**
     * Sets the active speaking flags.
     *
     * @param speakingFlags An EnumSet of SpeakingFlags representing speaking modes.
     */
    public void setSpeakingFlags(EnumSet<SpeakingFlag> speakingFlags) {
        if (!speakingFlags.equals(this.speakingFlags)) {
            this.speakingFlags = speakingFlags;
            webSocket.sendSpeakingUpdate();
        }
    }

    @Override
    public VoiceConnectionStatus getConnectionStatus() {
        return connectionStatus;
    }

    /**
     * Sets the status of the connection.
     *
     * @param status A VoiceConnectionStatus representing the status of the connection.
     */
    public void setConnectionStatus(VoiceConnectionStatus status) {
        this.connectionStatus = status;
    }

    @Override
    public Optional<AudioSource> getAudioSource() {
        return Optional.ofNullable(audioSource);
    }

    @Override
    public void setAudioSource(AudioSource source) {
        this.audioSource = source;
    }

    @Override
    public ServerVoiceChannel getConnectedChannel() {
        return connectedChannel;
    }

    /**
     * Sets the currently connected channel.
     *
     * @param channel The channel which this connection is connected to.
     */
    public void setConnectedChannel(ServerVoiceChannel channel) {
        this.connectedChannel = channel;
    }

    @Override
    public Server getServer() {
        return getConnectedChannel().getServer();
    }

    /**
     * Gets the AudioWebSocket of this connection.
     *
     * @return The AudioWebSocket for this connection.
     */
    public AudioWebSocketAdapter getWebSocket() {
        return webSocket;
    }

    /**
     * Gets the AudioUdpSocket of this connection.
     *
     * @return The AudioUdpSocket for this connection.
     */
    public AudioUdpSocket getUdpSocket() {
        return udpSocket;
    }

    /**
     * Sets the AudioUdpSocket for this channel.
     *
     * @param socket The AudioUdpSocket.
     */
    public void setUdpSocket(AudioUdpSocket socket) {
        this.udpSocket = socket;
    }

    public CompletableFuture<AudioConnection> getConnectedFuture() {
        return connectedFuture;
    }

}
