package org.javacord.core.event.server.voice;

import org.javacord.api.entity.channel.ServerVoiceChannel;
import org.javacord.api.event.server.voice.VoiceStateUpdateEvent;
import org.javacord.core.event.channel.server.voice.ServerVoiceChannelMemberEventImpl;

public class VoiceStateUpdateEventImpl extends ServerVoiceChannelMemberEventImpl implements VoiceStateUpdateEvent {

    private final String sessionId;

    /**
     * Constructs a new VoiceStateUpdateEventImpl instance.
     *
     * @param channel   The channel this voice server update is for.
     * @param userId    The id of the user this voice server update is for.
     * @param sessionId The session id for this user.
     */
    public VoiceStateUpdateEventImpl(ServerVoiceChannel channel, long userId, String sessionId) {
        super(userId, channel);
        this.sessionId = sessionId;
    }

    @Override
    public String getSessionId() {
        return sessionId;
    }

}
