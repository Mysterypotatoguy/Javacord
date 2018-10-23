package org.javacord.core.audio.source;

import org.apache.logging.log4j.Logger;
import org.javacord.api.audio.source.FixedLengthAudioSource;
import org.javacord.core.util.logging.LoggerUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class FileAudioSource implements FixedLengthAudioSource {

    private static final int BYTES_PER_FRAME = 900;
    private static final int MS_PER_FRAME = 20;
    private static final Logger LOGGER = LoggerUtil.getLogger(FileAudioSource.class);

    private File file;
    private FileInputStream inputStream;
    private int offset = 0;

    /**
     * Creates a new instance of a FileAudioSource.
     *
     * @param file The file to take audio data from.
     */
    public FileAudioSource(File file) {
        this.file = file;
        try {
            inputStream = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            LOGGER.error("Attempted to create file audio source from inexistant file", e);
        }
    }

    @Override
    public byte[] pollNextFrame() {
        byte[] frame = new byte[BYTES_PER_FRAME];
        try {
            int bytesRead = inputStream.read(frame);
            if (bytesRead < BYTES_PER_FRAME) {
                throw new AssertionError("Insufficient bytes to read despite prior check.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        offset += BYTES_PER_FRAME;
        return frame;
    }

    @Override
    public boolean hasNextFrame() {
        return offset <= file.length();
    }

    @Override
    public void stop() {
        try {
            inputStream.close();
        } catch (IOException e) {
            LOGGER.warn("Exception while closing file audio source", e);
        }
    }

    @Override
    public long getLength(TimeUnit unit) {
        long durationInMilliSeconds = file.length() * MS_PER_FRAME / BYTES_PER_FRAME;
        return TimeUnit.MILLISECONDS.convert(durationInMilliSeconds, unit);
    }

    @Override
    public long getPlayed(TimeUnit unit) {
        long timePlayedInMilliSeconds = offset * MS_PER_FRAME / BYTES_PER_FRAME;
        return TimeUnit.MILLISECONDS.convert(timePlayedInMilliSeconds, unit);
    }
}
