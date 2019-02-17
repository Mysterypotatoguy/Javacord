package org.javacord.api.event.server.voice;

import org.javacord.api.entity.user.User;
import org.javacord.api.event.channel.server.voice.ServerVoiceChannelEvent;
import org.javacord.api.event.user.UserEvent;

public interface VoiceStateUpdateEvent extends ServerVoiceChannelEvent, UserEvent {

    /**
     * Gets the user this voice state is for.
     *
     * @return The user.
     */
    User getUser();

    /**
     * Gets the session id provided in this event.
     *
     * @return The session id.
     */
    String getSessionId();

}
