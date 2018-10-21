package org.javacord.api.audio;

import org.javacord.api.audio.source.AudioSource;
import org.javacord.api.entity.channel.ServerVoiceChannel;
import org.javacord.api.entity.server.Server;

import java.util.EnumSet;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface VoiceConnection {

    /**
     * Moves the voice connection to the given voice channel.
     *
     * @param voiceChannel The voice channel to move to.
     * @return A future indicating the success of the action.
     */
    CompletableFuture<VoiceConnection> moveTo(ServerVoiceChannel voiceChannel);

    /**
     * Moves the voice connection to the given voice channel.
     *
     * @param voiceChannel The voice channel to move to.
     * @param selfMute     Whether or not to be self-muted on join.
     * @param selfDeafen   Whether or not to be self-deafened on join.
     * @return A future indicating the success of the action.
     */
    CompletableFuture<VoiceConnection> moveTo(ServerVoiceChannel voiceChannel, boolean selfMute, boolean selfDeafen);

    /**
     * Disconnects from the current voice channel.
     *
     * @return A future indicating the success of the action.
     */
    CompletableFuture<Void> disconnect();

    /**
     * Gets the self-muted status of this connection.
     *
     * @return Whether or not the connection is self-muted.
     */
    boolean isSelfMuted();

    /**
     * Sets the self-muted status of this connection.
     *
     * @param muted Whether or not to self-mute this connection.
     */
    void setSelfMuted(boolean muted);

    /**
     * Gets the self-deafened status of this connection.
     *
     * @return Whether or not the connection is self-deafened.
     */
    boolean isSelfDeafened();

    /**
     * Sets the self-deafened status of this connection.
     *
     * @param deafened Whether or not to self-deafen this connection.
     */
    void setSelfDeafened(boolean deafened);

    /**
     * Gets the priority speaking status of this connection.
     *
     * @return Whether or not the connection will speak with priority speaking.
     */
    boolean isPrioritySpeaking();

    /**
     * Sets the priority speaking status of this connection.
     *
     * @param prioritySpeaker Whether or not to speak with priority speaking.
     */
    void setPrioritySpeaking(boolean prioritySpeaker);

    /**
     * Gets a set of all the speaking flags currently being used.
     *
     * @return A set of all enums representing the speaking flags currently being used.
     */
    EnumSet<SpeakingFlag> getSpeakingFlags();

    /**
     * Gets the status of this connection.
     *
     * @return A VoiceConnectionStatus representing the status.
     */
    VoiceConnectionStatus getConnectionStatus();

    /**
     * Gets the AudioSource being used by this connection.
     *
     * @return The AudioSource currently being used by this connection.-
     */
    Optional<AudioSource> getAudioSource();

    /**
     * Sets the AudioSource to be used by this connection.
     *
     * @param source The AudioSource to use.
     */
    void setAudioSource(AudioSource source);

    /**
     * Gets the channel that this connection is connected to.
     *
     * @return The channel this connection is connected to.
     */
    ServerVoiceChannel getConnectedChannel();

    /**
     * Gets the server of the channel that this connection is connected to.
     *
     * @return The server of the channel that this connection is connected to.
     */
    Server getServer();

    /**
     * An enum representing the statuses that a connection can have.
     */
    enum VoiceConnectionStatus {
        CONNECTING, CONNECTED, RECONNECTING, DISCONNECTED
    }

}
