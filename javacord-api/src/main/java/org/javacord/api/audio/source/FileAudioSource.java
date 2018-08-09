package org.javacord.api.audio.source;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class FileAudioSource implements AudioSource {

    private static final int BUFFER_SIZE = 900;

    private File file;
    private FileInputStream inputStream;
    private int offset = 0;

    public FileAudioSource(File file) {
        this.file = file;
        try {
            inputStream = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public byte[] pollNextFrame() {
        byte[] frame = new byte[BUFFER_SIZE];
        try {
            inputStream.read(frame);
        } catch (IOException e) {
            e.printStackTrace();
        }
        offset += BUFFER_SIZE;
        return frame;
    }

    @Override
    public boolean hasNextFrame() {
        return offset <= file.length();
    }

}
