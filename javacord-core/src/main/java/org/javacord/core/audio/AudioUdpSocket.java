package org.javacord.core.audio;

import org.javacord.api.audio.SpeakingFlag;
import org.javacord.api.audio.source.AudioSource;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.EnumSet;

public class AudioUdpSocket {

    private DatagramSocket socket;

    private InetSocketAddress address;

    private Thread sendThread;
    //private Thread receiveThread;

    private int ssrc;
    private byte[] secretKey;
    private char sequence = (char) 0;
    private int timestamp = 0;

    private ImplVoiceConnection voiceConnection;

    public AudioUdpSocket(ImplVoiceConnection voiceConnection, InetSocketAddress address, int ssrc) {
        this.voiceConnection = voiceConnection;
        this.address = address;
        this.ssrc = ssrc;
        try {
            this.socket = new DatagramSocket();
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    public void disconnect() {
        stopSendingThread();
        socket.close();
    }

    public InetSocketAddress findLocalUdpAddress() { // TODO: Rename
        try {
            byte[] data = ByteBuffer.allocate(70).putInt(ssrc).array();
            socket.send(new DatagramPacket(data, data.length, address));

            DatagramPacket recvPacket = new DatagramPacket(new byte[70], 70);
            socket.receive(recvPacket);

            byte[] recvData = recvPacket.getData();
            String ip = new String(Arrays.copyOfRange(recvData, 3, recvData.length - 2)).trim();
            int port = ByteBuffer.wrap(new byte[]{recvData[69], recvData[68]}).getShort() & 0xFFFF;

            return new InetSocketAddress(ip, port);
        } catch (IOException exception) {
            exception.printStackTrace();
            // Fail to connect?
            return null;
        }
    }

    public void startSendingThread() {
        sendThread = new Thread(() -> {
                    long lastFrameTimestamp = System.nanoTime();
                    int silenceFramesToSend = 0;
                    while (!sendThread.isInterrupted()) {
                        if (!voiceConnection.getAudioSource().isPresent() || System.nanoTime() - lastFrameTimestamp < 20000000L) {
                            try {
                                Thread.sleep((20000000L - (System.nanoTime() - lastFrameTimestamp)) / 1000000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                        AudioSource source = voiceConnection.getAudioSource().get();
                        if (source.hasNextFrame()) {
                            byte[] audioFrame = source.pollNextFrame();
                            try {
                                setSpeaking(true);
                                DatagramPacket audioPacket = new AudioPacket(audioFrame, ssrc, sequence, timestamp)
                                        .encrypt(secretKey)
                                        .asUdpPacket(address);
                                socket.send(audioPacket);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            sequence++;
                            timestamp += 960;
                            silenceFramesToSend = 5;
                        } else {
                            if (silenceFramesToSend > 0) {
                                sendSilenceFrame();
                                silenceFramesToSend--;
                            } else {
                                setSpeaking(false);
                            }
                        }
                        lastFrameTimestamp = System.nanoTime();
                    }
                });
        sendThread.setName("Javacord Audio Send Thread (Server: " + voiceConnection.getServer().getId() + ")");
        sendThread.setDaemon(true);
        sendThread.start();
    }

    public void stopSendingThread() {
        if (sendThread != null && sendThread.isAlive()) {
            sendThread.interrupt();
        }
    }

    /*public void startReceivingThread() {
        receiveService.execute(() -> {
            while (true) {
                try {
                    DatagramPacket recvPacket = new DatagramPacket(new byte[960], 960);
                    socket.receive(recvPacket);
                    byte[] data = recvPacket.getData();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }*/

    private void sendSilenceFrame() {
        try {
            AudioPacket silencePacket = new AudioPacket(null, ssrc, sequence, timestamp).encrypt(secretKey);
            socket.send(silencePacket.asUdpPacket(address));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setSpeaking(boolean speaking) {
        EnumSet<SpeakingFlag> speakingFlags = voiceConnection.getSpeakingFlags();
        if (speaking) {
            speakingFlags.add(SpeakingFlag.SPEAKING);
        } else {
            speakingFlags.remove(SpeakingFlag.SPEAKING);
        }
        voiceConnection.setSpeakingFlags(speakingFlags);
    }

    public void setSecretKey(byte[] secretKey) {
        this.secretKey = secretKey;
    }

    public InetSocketAddress getAddress() {
        return address;
    }

    public int getSsrc() {
        return ssrc;
    }
}
