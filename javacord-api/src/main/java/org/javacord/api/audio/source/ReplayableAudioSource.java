package org.javacord.api.audio.source;

/**
 * An audio source that can be replayed.
 *
 * <p>The Audio source needs to be reset in order to be replayed. The content will not change between resets.
 */
public interface ReplayableAudioSource extends AudioSource {

    /**
     * Reset the audio source.
     */
    void reset();

}
