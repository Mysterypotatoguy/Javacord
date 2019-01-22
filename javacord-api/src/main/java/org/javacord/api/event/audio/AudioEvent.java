package org.javacord.api.event.audio;

import org.javacord.api.audio.AudioConnection;
import org.javacord.api.event.Event;

public interface AudioEvent extends Event {

    /**
     * Gets the audio connection of the event.
     *
     * @return The audio connection of the event.
     */
    AudioConnection getConnection();
}
