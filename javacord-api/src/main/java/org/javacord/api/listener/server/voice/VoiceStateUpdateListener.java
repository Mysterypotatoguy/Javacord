package org.javacord.api.listener.server.voice;

import org.javacord.api.event.server.voice.VoiceStateUpdateEvent;
import org.javacord.api.listener.GloballyAttachableListener;
import org.javacord.api.listener.ObjectAttachableListener;
import org.javacord.api.listener.channel.server.ServerChannelAttachableListener;

@FunctionalInterface
public interface VoiceStateUpdateListener extends ServerChannelAttachableListener,
        GloballyAttachableListener, ObjectAttachableListener {

    /**
     * This method is called every time a voice state update packet is received.
     *
     * @param event The event.
     */
    void onVoiceStateUpdate(VoiceStateUpdateEvent event);

}
