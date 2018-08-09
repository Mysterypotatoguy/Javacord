package org.javacord.core.audio;

import org.javacord.api.audio.source.AudioSource;
import org.javacord.core.util.concurrent.ThreadFactory;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class AudioUdpSocket {

    private DatagramSocket socket;

    private InetSocketAddress address;

    private Thread sendThread;
    //private Thread receiveThread;

    private int ssrc;
    private byte[] secretKey;
    private char sequence = (char) 0;
    private int timestamp = 0;

    private boolean isSpeaking;

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
        sendThread = new ThreadFactory("Javacord Audio Thread (Server: " + voiceConnection.getServer().getId() + ")", true)
                .newThread(() -> {
                    long lastFrameTimestamp = System.nanoTime();
                    int silenceFramesToSend = 0;
                    while (!sendThread.isInterrupted()) {
                        if (!voiceConnection.getAudioSource().isPresent() || System.nanoTime() - lastFrameTimestamp < 20000000L) {
                            continue;
                        }
                        AudioSource source = voiceConnection.getAudioSource().get();
                        if (source.hasNextFrame()) {
                            byte[] audioFrame = source.pollNextFrame();
                            try {
                                if (!isSpeaking) {
                                    setSpeaking(true);
                                }
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
                            } else if (isSpeaking) {
                                setSpeaking(false);
                            }
                        }
                        lastFrameTimestamp = System.nanoTime();
                    }
                });
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
        isSpeaking = speaking;
        voiceConnection.getWebSocket().sendSpeakingUpdate(speaking);
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
