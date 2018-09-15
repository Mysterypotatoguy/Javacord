package org.javacord.api.audio;

import org.javacord.api.audio.source.AudioSource;
import org.javacord.api.entity.channel.ServerVoiceChannel;
import org.javacord.api.entity.server.Server;

import java.util.EnumSet;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface VoiceConnection {

    CompletableFuture<VoiceConnection> moveTo(ServerVoiceChannel voiceChannel);

    CompletableFuture<VoiceConnection> moveTo(ServerVoiceChannel voiceChannel, boolean selfMute, boolean selfDeafen);

    CompletableFuture<Void> disconnect();

    boolean isSelfMuted();

    void setSelfMuted(boolean muted);

    boolean isSelfDeafened();

    void setSelfDeafened(boolean deafened);

    boolean isPrioritySpeaking();

    void setPrioritySpeaking(boolean prioritySpeaker);

    EnumSet<SpeakingFlag> getSpeakingFlags();

    VoiceConnectionStatus getConnectionStatus();

    Optional<AudioSource> getAudioSource();

    void setAudioSource(AudioSource source);

    ServerVoiceChannel getConnectedChannel();

    Server getServer();

    enum VoiceConnectionStatus {
        CONNECTING, CONNECTED, RECONNECTING, DISCONNECTED
    }

}
