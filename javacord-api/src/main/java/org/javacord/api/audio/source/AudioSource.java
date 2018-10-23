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
     * Stop this audio source.
     *
     * <p>Any remaining audio frames may be dropped, underlying connections and caches be cleaned up
     */
    void stop();

    /**
     * Gets the name if the source is nameable.
     *
     * @return The sources' name if possible.
     */
    default Optional<String> tryGetName() {
        return as(NameableAudioSource.class).map(NameableAudioSource::getName);
    }

    /**
     * Gets the source as a pausable audio source.
     *
     * @return The source as a pausable source.
     */
    default Optional<PausableAudioSource> asPausableAudioSource() {
        return as(PausableAudioSource.class);
    }

    /**
     * Gets the source as a seekable audio source.
     *
     * @return The source as a seekable source.
     */
    default Optional<SeekableAudioSource> asSeekableAudioSource() {
        return as(SeekableAudioSource.class);
    }

    /**
     * Gets the source as an audio source of fixed length.
     *
     * @return The source as a fixed length source.
     */
    default Optional<FixedLengthAudioSource> asFixedLengthAudioSource() {
        return as(FixedLengthAudioSource.class);
    }

    /**
     * Gets the source as a replayble audio source.
     *
     * @return The source as a replayable audio source.
     */
    default Optional<ReplayableAudioSource> asReplayableAudioSource() {
        return as(ReplayableAudioSource.class);
    }



}
