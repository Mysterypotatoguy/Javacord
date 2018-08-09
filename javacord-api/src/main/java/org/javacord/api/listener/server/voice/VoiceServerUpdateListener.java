package org.javacord.api.listener.server.voice;

import org.javacord.api.event.server.voice.VoiceServerUpdateEvent;
import org.javacord.api.listener.GloballyAttachableListener;
import org.javacord.api.listener.ObjectAttachableListener;
import org.javacord.api.listener.server.ServerAttachableListener;

@FunctionalInterface
public interface VoiceServerUpdateListener extends ServerAttachableListener,
        GloballyAttachableListener, ObjectAttachableListener {

    void onVoiceServerUpdate(VoiceServerUpdateEvent event);

}