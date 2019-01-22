package org.javacord.api.event.audio.source;

import org.javacord.api.audio.source.AudioSource;
import org.javacord.api.event.audio.AudioEvent;

public interface AudioSourceStartEvent extends AudioEvent {

    /**
     * Gets the audio source which has started.
     *
     * @return The audio source which has started.
     */
    AudioSource getSource();
}
