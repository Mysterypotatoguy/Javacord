package org.javacord.api.audio.source;

import java.util.Optional;

public interface AudioSource {

    /**
     * Polls for the next 20ms of audio from the source.
     *
     * @return A byte array containing 20ms of audio, or null if hasNextFrame is false.
     */
    byte[] pollNextFrame();

    /**
     * Checks whether there is 20ms of audio available to be polled.
     *
     * @return Whether or not there is a frame available to be polled.
     */
    boolean hasNextFrame();

    /**
     * Gets the source as a pausable audio source.
     * @return The source as a pausable source.
     */
    default Optional<PausableAudioSource> asPausableAudioSource() {
        if (this instanceof PausableAudioSource) {
            return Optional.of((PausableAudioSource) this);
        }
        return Optional.empty();
    }

    /**
     * Gets the source as a persistent audio source.
     * @return The source as a persistent source.
     */
    default Optional<PersistentAudioSource> asPersistentAudioSource() {
        if (this instanceof PausableAudioSource) {
            return Optional.of((PersistentAudioSource) this);
        }
        return Optional.empty();
    }

}
