package de.btobastian.javacord.entities;

import de.btobastian.javacord.entities.channels.ServerChannel;
import de.btobastian.javacord.entities.channels.ServerTextChannel;
import de.btobastian.javacord.entities.channels.ServerVoiceChannel;
import de.btobastian.javacord.listeners.message.MessageCreateListener;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * The class represents a Discord server, sometimes also called guild.
 */
public interface Server extends DiscordEntity {

    /**
     * Gets the name of the server.
     *
     * @return The name of the server.
     */
    String getName();

    /**
     * Gets a collection with all channels of the server.
     *
     * @return A collection with all channels of the server.
     */
    Collection<ServerChannel> getChannels();

    /**
     * Gets a collection with all text channels of the server.
     *
     * @return A collection with all text channels of the server.
     */
    default Collection<ServerTextChannel> getTextChannels() {
        return getChannels().stream()
                .filter(channel -> channel instanceof ServerTextChannel)
                .map(channel -> (ServerTextChannel) channel)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * Gets a collection with all voice channels of the server.
     *
     * @return A collection with all voice channels of the server.
     */
    default Collection<ServerVoiceChannel> getVoiceChannels() {
        return getChannels().stream()
                .filter(channel -> channel instanceof ServerVoiceChannel)
                .map(channel -> (ServerVoiceChannel) channel)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * Gets a channel by it's id.
     *
     * @param id The id of the channel.
     * @return The channel with the given id.
     */
    Optional<ServerChannel> getChannelById(long id);

    /**
     * Gets a channel by it's id.
     *
     * @param id The id of the channel.
     * @return The channel with the given id.
     */
    default Optional<ServerChannel> getChannelById(String id) {
        try {
            return getChannelById(Long.valueOf(id));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    /**
     * Gets a text channel by it's id.
     *
     * @param id The id of the text channel.
     * @return The text channel with the given id.
     */
    default Optional<ServerTextChannel> getTextChannelById(long id) {
        return getChannelById(id)
                .filter(channel -> channel instanceof ServerTextChannel)
                .map(channel -> (ServerTextChannel) channel);
    }

    /**
     * Gets a text channel by it's id.
     *
     * @param id The id of the text channel.
     * @return The text channel with the given id.
     */
    default Optional<ServerTextChannel> getTextChannelById(String id) {
        try {
            return getTextChannelById(Long.valueOf(id));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    /**
     * Gets a voice channel by it's id.
     *
     * @param id The id of the voice channel.
     * @return The voice channel with the given id.
     */
    default Optional<ServerVoiceChannel> getVoiceChannelById(long id) {
        return getChannelById(id)
                .filter(channel -> channel instanceof ServerVoiceChannel)
                .map(channel -> (ServerVoiceChannel) channel);
    }

    /**
     * Gets a voice channel by it's id.
     *
     * @param id The id of the voice channel.
     * @return The voice channel with the given id.
     */
    default Optional<ServerVoiceChannel> getVoiceChannelById(String id) {
        try {
            return getVoiceChannelById(Long.valueOf(id));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    /**
     * Adds a listener, which listens to message creates in this server.
     *
     * @param listener The listener to add.
     */
    void addMessageCreateListener(MessageCreateListener listener);

    /**
     * Gets a list with all registered message create listeners.
     *
     * @return A list with all registered message create listeners.
     */
    List<MessageCreateListener> getMessageCreateListeners();

}