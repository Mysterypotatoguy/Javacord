package org.javacord.core.audio;

import org.javacord.api.audio.SpeakingFlag;
import org.javacord.api.audio.VoiceConnection;
import org.javacord.api.audio.source.AudioSource;
import org.javacord.api.entity.channel.ServerVoiceChannel;
import org.javacord.api.entity.server.Server;
import org.javacord.core.DiscordApiImpl;

import java.util.EnumSet;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class ImplVoiceConnection implements VoiceConnection {

    private DiscordApiImpl api;
    private ServerVoiceChannel connectedChannel;

    private boolean selfMuted = false;
    private boolean selfDeafened = false;

    private EnumSet<SpeakingFlag> speakingFlags = EnumSet.of(SpeakingFlag.NONE);

    private VoiceConnectionStatus connectionStatus = VoiceConnectionStatus.CONNECTING;

    private AudioWebSocketAdapter webSocket;
    private AudioUdpSocket udpSocket;

    private AudioSource audioSource;
    // private List<> outputs;

    public ImplVoiceConnection(DiscordApiImpl api, ServerVoiceChannel channel, String endpoint, String token) {
        this.api = api;
        this.connectedChannel = channel;
        webSocket = new AudioWebSocketAdapter(api, this, endpoint, token);
        webSocket.connect();
    }

    public void reconnect(ServerVoiceChannel channel, String endpoint, String token) {
        disconnect();
        webSocket = new AudioWebSocketAdapter(api, this, endpoint, token);
        webSocket.connect();
        setConnectedChannel(channel);
    }

    @Override
    public CompletableFuture<VoiceConnection> moveTo(ServerVoiceChannel voiceChannel) {
        return moveTo(voiceChannel, selfMuted, selfDeafened);
    }

    @Override
    public CompletableFuture<VoiceConnection> moveTo(ServerVoiceChannel voiceChannel, boolean selfMute, boolean selfDeafen) {
        if (connectionStatus == VoiceConnectionStatus.DISCONNECTED) {
            CompletableFuture<VoiceConnection> future = new CompletableFuture<>();
            future.completeExceptionally(new IllegalStateException("VoiceConnection is disconnected!"));
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
            future.completeExceptionally(new IllegalStateException("VoiceConnection is disconnected!"));
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

    public void setConnectedChannel(ServerVoiceChannel channel) {
        this.connectedChannel = channel;
    }

    @Override
    public Server getServer() {
        return getConnectedChannel().getServer();
    }

    public AudioWebSocketAdapter getWebSocket() {
        return webSocket;
    }

    public AudioUdpSocket getUdpSocket() {
        return udpSocket;
    }

    public void setUdpSocket(AudioUdpSocket socket) {
        this.udpSocket = socket;
    }

}
