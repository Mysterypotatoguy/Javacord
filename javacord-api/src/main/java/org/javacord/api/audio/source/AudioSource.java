package org.javacord.api.audio.source;

import java.util.Optional;

public interface AudioSource {

    byte[] pollNextFrame();

    boolean hasNextFrame();

    default Optional<PausableAudioSource> asPausableAudioSource() {
        if (this instanceof PausableAudioSource) {
            return Optional.of((PausableAudioSource) this);
        }
        return Optional.empty();
    }

    default Optional<PersistentAudioSource> asPersistentAudioSource() {
        if (this instanceof PausableAudioSource) {
            return Optional.of((PersistentAudioSource) this);
        }
        return Optional.empty();
    }

}
