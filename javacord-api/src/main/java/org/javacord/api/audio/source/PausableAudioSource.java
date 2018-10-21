package org.javacord.api.audio.source;

public interface PausableAudioSource extends AudioSource {

    /**
     * Pauses the audio source (Makes hasNextFrame return false).
     */
    void pause();

    /**
     * Resumes the audio source (Makes hasNextFrame return true if there is still data).
     */
    void resume();

}
