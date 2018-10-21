package org.javacord.core.event.server.voice;

import org.javacord.api.entity.server.Server;
import org.javacord.api.event.server.voice.VoiceServerUpdateEvent;
import org.javacord.core.event.server.ServerEventImpl;

public class VoiceServerUpdateEventImpl extends ServerEventImpl implements VoiceServerUpdateEvent {
    private String token;
    private String endpoint;

    /**
     * Constructs a new VoiceServerUpdateEventImpl instance.
     *
     * @param server   The server this voice server update is for.
     * @param token    The voice token to provide when opening a voice websocket.
     * @param endpoint The endpoint to connect the voice websocket to.
     */
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