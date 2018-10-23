package org.javacord.api.audio.source;

import java.util.concurrent.TimeUnit;

/**
 * An audio source that can jump to a specific time mark.
 */
public interface SeekableAudioSource extends AudioSource {

    /**
     * Jump to a specific mark.
     *
      * @param mark The duration to jump to, measured from the beginning.
     * @param unit The unit of the duration.
     * @throws RuntimeException some yet to determined exception if the mark given is out of bounds.
     */
    void jumpTo(long mark, TimeUnit unit);

    /**
     * Gets how many seconds (rounded down) of the source have been played already.
     *
     * @return The number of seconds the source has been playing for.
     */
    default long getPositionInSeconds() {
        return getPosition(TimeUnit.SECONDS);
    }

    /**
     * Gets the duration the source has been played already in any time unit (rounded down).
     *
     * @param unit The desired time unit.
     * @return The duration the source has been playing for in the given time unit.
     */
    long getPosition(TimeUnit unit);

    /**
     * Jump forward by a given time frame.
     *
     * @param duration The duration.
     * @param unit The unit of the duration.
     * @throws RuntimeException as in {@link #jumpTo(long, TimeUnit)}
     */
    default void forward(long duration, TimeUnit unit) {
        jumpTo(getPosition(unit) + duration, unit);
    }

    /**
     * Jump backward by a given time frame.
     *
     * @param duration The duration.
     * @param unit The unit of the duration.
     * @throws RuntimeException as in {@link #jumpTo(long, TimeUnit)}
     */
    default void backward(long duration, TimeUnit unit) {
        jumpTo(getPosition(unit) - duration, unit);
    }

}
