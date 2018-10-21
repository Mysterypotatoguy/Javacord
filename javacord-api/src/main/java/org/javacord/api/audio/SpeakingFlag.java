package org.javacord.api.audio;

/**
 * An enum with all speaking flags.
 */
public enum SpeakingFlag {

    NONE(0),
    SPEAKING(1),
    SOUNDSHARE(2),
    PRIORITY_SPEAKER(4);

    private int flag;

    /**
     * Creates a new speaking flag.
     *
     * @param flag The numerical code for this flag
     */
    SpeakingFlag(int flag) {
        this.flag = flag;
    }

    /**
     * Gets the numerical code for the flag.
     *
     * @return The numerical code.
     */
    public int getFlag() {
        return flag;
    }
}
