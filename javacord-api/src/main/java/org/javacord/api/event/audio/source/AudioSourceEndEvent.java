package org.javacord.api.event.audio.source;

import org.javacord.api.audio.source.AudioSource;
import org.javacord.api.event.audio.AudioEvent;

public interface AudioSourceEndEvent extends AudioEvent {

    /**
     * Gets the audio source which has ended.
     *
     * @return The audio source which has ended.
     */
    AudioSource getSource();
}
