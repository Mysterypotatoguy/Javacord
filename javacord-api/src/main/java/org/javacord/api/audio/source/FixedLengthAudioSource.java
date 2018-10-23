package org.javacord.api.audio.source;

import java.util.concurrent.TimeUnit;

/**
 * An audio source of determinate length, like an audio file or a youtube video.
 */
public interface FixedLengthAudioSource extends AudioSource {

    /**
     * Gets the length of the clip in seconds (rounded down).
     *
     * @return The total length in seconds.
     */
    default long getLengthInSeconds() {
        return getLength(TimeUnit.SECONDS);
    }

    /**
     * Gets the length of the clip in any time unit (rounded down).
     *
     * @param unit The desired time unit.
     * @return The total length in the desired time unit.
     */
    long getLength(TimeUnit unit);

}
