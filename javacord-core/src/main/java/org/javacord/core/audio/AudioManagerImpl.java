package org.javacord.core.audio;

import org.javacord.api.audio.AudioConnection;
import org.javacord.api.audio.AudioManager;
import org.javacord.api.entity.channel.ServerVoiceChannel;
import org.javacord.api.entity.server.Server;
import org.javacord.api.listener.server.voice.VoiceServerUpdateListener;
import org.javacord.api.util.event.ListenerManager;
import org.javacord.core.DiscordApiImpl;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

public class AudioManagerImpl implements AudioManager {

    private DiscordApiImpl api;

    private HashMap<Long, CompletableFuture<AudioConnection>> pendingFutures = new HashMap<>();

    //Server ID, AudioConnection
    private HashMap<Long, AudioConnection> voiceConnections = new HashMap<>();

    /**
     * Creates a new AudioManager.
     *
     * @param api The DiscordApi instance.
     */
    public AudioManagerImpl(DiscordApiImpl api) {
        this.api = api;
    }

    /**
     * Starts a new voice connection for the given channel, or if a connection already exists for this channel return
     * it.
     *
     * @param channel The channel to connect to.
     * @param selfMute The self-mute state to have on join.
     * @param selfDeafen The self-deafen state to have on join.
     * @return A AudioConnection for this connection.
     */
    public CompletableFuture<AudioConnection> startNewConnection(ServerVoiceChannel channel,
                                                                 boolean selfMute,
                                                                 boolean selfDeafen) {
        CompletableFuture<AudioConnection> future = new CompletableFuture<>();
        if (voiceConnections.containsKey(channel.getServer().getId())) {
            //Already connected to channel in this guild
            if (voiceConnections.get(channel.getServer().getId()).getConnectedChannel().equals(channel)) {
                //Already connected to this channel
                future.complete(voiceConnections.get(channel.getId()));
                return future;
            }
            //But attempting to join a different channel (an intra-server move)
            return moveConnection(((AudioConnectionImpl) voiceConnections.get(channel.getServer().getId())), channel);
        }
        AtomicReference<ListenerManager<VoiceServerUpdateListener>> lm = new AtomicReference<>();
        lm.set(api.addListener(VoiceServerUpdateListener.class, event -> {
            if (event.getServer() != channel.getServer()) {
                return;
            }
            String endpoint = event.getEndpoint();
            String token = event.getToken();
            voiceConnections.put(channel.getServer().getId(), new AudioConnectionImpl(api, channel, endpoint, token));
            pendingFutures.put(channel.getId(), future);
            lm.get().remove();
        }));
        api.getWebSocketAdapter().sendVoiceStateUpdate(
                channel.getServer(),
                channel,
                selfMute,
                selfDeafen);
        return future;
    }

    /**
     * Moves an active voice connection from one channel to another.
     *
     * @param connection The AudioConnection to move.
     * @param destChannel The destination channel.
     * @return The AudioConnection.
     */
    public CompletableFuture<AudioConnection> moveConnection(AudioConnectionImpl connection,
                                                             ServerVoiceChannel destChannel) {
        CompletableFuture<AudioConnection> future = new CompletableFuture<>();
        if (destChannel.getServer().equals(connection.getServer())) {
            if (connection.getConnectedChannel().equals(destChannel)) {
                //We are already connected to this channel, so there's no need to do anything
                future.complete(voiceConnections.get(destChannel.getId()));
                return future;
            }
            //We're just moving to a different channel. Send a VStateUpdate
        } else {
            /*We're attempting to move cross-server
              Both the current server and destination server's connections need to be terminated
              Then we need to send a VStateUpdate and connect to the destination server, after receiving a VServerUpdate
            */
            AtomicReference<ListenerManager<VoiceServerUpdateListener>> lm = new AtomicReference<>();
            lm.set(api.addListener(VoiceServerUpdateListener.class, event -> {
                if (event.getServer() != destChannel.getServer()) {
                    return;
                }
                String endpoint = event.getEndpoint();
                String token = event.getToken();

                //Terminate destination (If there is one)
                getVoiceConnection(destChannel.getServer()).ifPresent(AudioConnection::disconnect);
                //Terminate & reconnect source to destination
                connection.reconnect(destChannel, endpoint, token);
                //Replace the old connection since we don't need it any more
                voiceConnections.replace(destChannel.getServer().getId(), connection);
                pendingFutures.put(destChannel.getId(), future);
                lm.get().remove();
            }));
        }
        api.getWebSocketAdapter().sendVoiceStateUpdate(
                destChannel.getServer(),
                destChannel,
                connection.isSelfMuted(),
                connection.isSelfDeafened());
        return future;
    }

    /**
     * Finishes a connection and returns the completed AudioConnection to the user.
     * This happens when the audio websocket receives a SESSION_DESCRIPTION (VOp 4) and signifies the AudioConnection
     * is complete and ready to receive events/send audio.
     *
     * @param serverId The ID of the server which this connection is connected to.
     * @param connection The AudioConnection to return.
     */
    public void completeConnection(Long serverId, AudioConnection connection) {
        CompletableFuture<AudioConnection> future = pendingFutures.get(serverId);
        //TODO: Check for disconnects between starting and completion.
        future.complete(connection);
    }

    /**
     * Removes a AudioConnection from the list of active voice connections.
     *
     * @param serverId The ID of the server to remove the connection for.
     */
    public void removeConnection(Long serverId) {
        voiceConnections.remove(serverId);
    }

    /**
     * Disconnects all active voice connections.
     */
    public void disconnectAll() {
        voiceConnections.values().forEach(AudioConnection::disconnect);
    }

    @Override
    public Collection<AudioConnection> getVoiceConnections() {
        return Collections.unmodifiableCollection(voiceConnections.values());
    }

    @Override
    public Optional<AudioConnection> getVoiceConnection(ServerVoiceChannel channel) {
        return Optional.ofNullable(voiceConnections.get(channel.getServer().getId()))
                .filter(connection -> connection.getConnectedChannel().equals(channel));
    }

    @Override
    public Optional<AudioConnection> getVoiceConnection(Server server) {
        return Optional.ofNullable(voiceConnections.get(server.getId()));
    }

}