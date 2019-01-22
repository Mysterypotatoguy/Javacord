package org.javacord.api.event.audio.source;

import org.javacord.api.audio.source.AudioSource;
import org.javacord.api.event.audio.AudioEvent;

public interface AudioSourcePauseEvent extends AudioEvent {

    /**
     * Gets the audio source which has been paused.
     *
     * @return The audio source which has been paused.
     */
    AudioSource getSource();
}
