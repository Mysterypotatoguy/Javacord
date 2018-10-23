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

    /**
     * Gets how many seconds (rounded down) of the source have been played already.
     *
     * @return The number of seconds the source has been playing for.
     */
    default long getSecondsPlayed() {
        return getPlayed(TimeUnit.SECONDS);
    }

    /**
     * Gets the duration the source has been played already in any time unit (rounded down).
     *
     * @param unit The desired time unit.
     * @return The duration the source has been playing for in the given time unit.
     */
    long getPlayed(TimeUnit unit);

    /**
     * Gets the remaining duration the source will be playing for in seconds (rounded down).
     *
     * @return The remaining duration of the source in seconds.
     */
    default long getRemainingSeconds() {
        return getRemainingTime(TimeUnit.SECONDS);
    }

    /**
     * Gets the remaining duration the source will be playing for in any time unit (rounded down).
     *
     * @param unit The desired time unit.
     * @return The remaining duration of the source in the given time unit.
     */
    default long getRemainingTime(TimeUnit unit) {
        return getLength(unit) - getPlayed(unit);
    }

}
