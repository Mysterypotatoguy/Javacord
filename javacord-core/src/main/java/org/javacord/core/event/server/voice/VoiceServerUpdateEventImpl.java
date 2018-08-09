package org.javacord.core.event.server.voice;

import org.javacord.api.entity.server.Server;
import org.javacord.api.event.server.voice.VoiceServerUpdateEvent;
import org.javacord.core.event.server.ServerEventImpl;

public class VoiceServerUpdateEventImpl extends ServerEventImpl implements VoiceServerUpdateEvent {
    private String token;
    private String endpoint;

    public VoiceServerUpdateEventImpl(Server server, String token, String endpoint) {
        super(server);
        this.token = token;
        this.endpoint = endpoint;
    }

    @Override
    public String getToken() {
        return token;
    }

    @Override
    public String getEndpoint() {
        return endpoint;
    }
}