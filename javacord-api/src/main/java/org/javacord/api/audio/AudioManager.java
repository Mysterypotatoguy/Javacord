package org.javacord.api.audio;

import org.javacord.api.entity.channel.ServerVoiceChannel;
import org.javacord.api.entity.server.Server;

import java.util.Collection;
import java.util.Optional;

public interface AudioManager {

    /**
     * Gets a collection with all active voice connections.
     *
     * @return A collection with all active voice connections.
     */
    Collection<AudioConnection> getVoiceConnections();

    /**
     * Gets the voice connection for a specified channel.
     *
     * @param channel The channel to get a voice connection for.
     * @return The voice connection associated with the specified voice channel.
     */
    Optional<AudioConnection> getVoiceConnection(ServerVoiceChannel channel);

    /**
     * Gets the voice connection for a specified channel.
     *
     * @param server The server to get a voice connection for.
     * @return The voice connection associated with the specified server.
     */
    Optional<AudioConnection> getVoiceConnection(Server server);

}
