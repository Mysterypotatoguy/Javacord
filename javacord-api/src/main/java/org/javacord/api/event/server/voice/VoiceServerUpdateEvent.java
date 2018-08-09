package org.javacord.api.event.server.voice;

import org.javacord.api.event.server.ServerEvent;

public interface VoiceServerUpdateEvent extends ServerEvent {

    String getToken();

    String getEndpoint();

}