package org.javacord.api.audio.source;

import org.javacord.api.util.Specializable;

import java.util.Optional;

public interface AudioSource extends Specializable<AudioSource> {

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
        return as(PausableAudioSource.class);
    }

    /**
     * Gets the source as a persistent audio source.
     * @return The source as a persistent source.
     */
    default Optional<PersistentAudioSource> asPersistentAudioSource() {
        return as(PersistentAudioSource.class);
    }



}
