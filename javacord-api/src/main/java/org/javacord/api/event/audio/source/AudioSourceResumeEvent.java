package org.javacord.api.event.audio.source;

import org.javacord.api.audio.source.AudioSource;
import org.javacord.api.event.audio.AudioEvent;

public interface AudioSourceResumeEvent extends AudioEvent {

    /**
     * Gets the audio source which has resumed.
     *
     * @return The audio source which has resumed.
     */
    AudioSource getSource();
}
