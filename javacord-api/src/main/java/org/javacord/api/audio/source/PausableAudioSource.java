package org.javacord.api.audio.source;

public interface PausableAudioSource extends AudioSource {

    void pause();

    void resume();

}
