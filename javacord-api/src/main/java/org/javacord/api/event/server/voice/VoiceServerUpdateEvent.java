package org.javacord.api.event.server.voice;

import org.javacord.api.event.server.ServerEvent;

public interface VoiceServerUpdateEvent extends ServerEvent {

    /**
     * Gets the voice token provided in this event.
     *
     * @return The voice token.
     */
    String getToken();

    /**
     * Gets the endpoint which should be connected to.
     *
     * @return The endpoint to connect to.
     */
    String getEndpoint();

}