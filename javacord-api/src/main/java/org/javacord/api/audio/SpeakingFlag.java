package org.javacord.api.audio;

public enum SpeakingFlag {

    NONE(0),
    SPEAKING(1),
    SOUNDSHARE(2),
    PRIORITY_SPEAKER(4);

    private int flag;

    SpeakingFlag(int flag) {
        this.flag = flag;
    }

    public int getFlag() {
        return flag;
    }
}
