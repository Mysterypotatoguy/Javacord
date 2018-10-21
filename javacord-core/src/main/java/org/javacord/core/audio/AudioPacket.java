package org.javacord.core.audio;

import com.codahale.xsalsa20poly1305.SecretBox;
import okio.ByteString;

import java.net.DatagramPacket;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

public class AudioPacket {

    private static final byte[] SILENCE_FRAME = {(byte) 0xF8, (byte) 0xFF, (byte) 0xFE};
    private static final byte RTP_TYPE = (byte) 0x80;
    private static final byte RTP_VERSION = (byte) 0x78;
    private static final int RTP_HEADER_LENGTH = 12;
    private static final int NONCE_LENGTH = 24;
    private boolean encrypted;
    private byte[] header;
    private byte[] audioFrame;

    /**
     * Constructs a new AudioPacket instance.
     *
     * @param audioFrame A byte array containing 20ms of audio.
     * @param ssrc       The SSRC.
     * @param sequence   The sequence number.
     * @param timestamp  The timestamp.
     */
    public AudioPacket(byte[] audioFrame, int ssrc, char sequence, int timestamp) {
        if (audioFrame == null) {
            audioFrame = SILENCE_FRAME;
        }
        this.audioFrame = audioFrame;
        ByteBuffer buffer = ByteBuffer.allocate(RTP_HEADER_LENGTH)
                .put(0, RTP_TYPE)
                .put(1, RTP_VERSION)
                .putChar(2, sequence)
                .putInt(4, timestamp)
                .putInt(8, ssrc);
        header = buffer.array();
    }

    /**
     * Encrypts the packet using the given key.
     *
     * @param key The key to encrypt with.
     * @return The AudioPacket with the audio frame encrypted.
     */
    public AudioPacket encrypt(byte[] key) {
        byte[] nonce = new byte[NONCE_LENGTH];
        System.arraycopy(header, 0, nonce, 0, RTP_HEADER_LENGTH);
        audioFrame = new SecretBox(ByteString.of(key)).seal(ByteString.of(nonce), ByteString.of(audioFrame))
                .toByteArray();
        encrypted = true;
        return this;
    }

    /**
     * Packs the AudioPacket into a DatagramPacket.
     *
     * @param address The destination address.
     * @return The AudioPacket as a DatagramPacket.
     */
    public DatagramPacket asUdpPacket(InetSocketAddress address) {
        byte[] packet = new byte[header.length + audioFrame.length];
        System.arraycopy(header, 0, packet, 0, header.length);
        System.arraycopy(audioFrame, 0, packet, header.length, audioFrame.length);
        return new DatagramPacket(packet, packet.length, address);
    }

    /**
     * Gets whether or not the packet has been encrypted.
     *
     * @return Whether or not the packet has been encrypted.
     */
    public boolean isEncrypted() {
        return encrypted;
    }

}
