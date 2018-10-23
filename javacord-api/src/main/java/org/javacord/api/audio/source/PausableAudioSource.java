package org.javacord.api.audio.source;

/**
 * An audio source that can be paused and resumed without dropping any frames in between.
 */
public interface PausableAudioSource extends AudioSource {

    /**
     * Check whether the audio source is paused.
     *
     * @return Whether the audio source is paused.
     */
    boolean isPaused();

    /**
     * Pauses the audio source (Makes hasNextFrame return false).
     */
    void pause();

    /**
     * Resumes the audio source (Makes hasNextFrame return true if there is still data).
     */
    void resume();

}
