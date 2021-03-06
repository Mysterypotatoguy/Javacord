package org.javacord.api.entity.message.internal;

import org.javacord.api.entity.Icon;
import org.javacord.api.entity.Mentionable;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.message.MessageDecoration;
import org.javacord.api.entity.message.Messageable;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.user.User;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.concurrent.CompletableFuture;

/**
 * This class is internally used by the {@link MessageBuilder} to create messages.
 * You usually don't want to interact with this object.
 */
public interface MessageBuilderDelegate {

    /**
     * Appends code to the message.
     *
     * @param language The language, e.g. "java".
     * @param code The code.
     */
    void appendCode(String language, String code);

    /**
     * Appends a sting with or without decoration to the message.
     *
     * @param message The string to append.
     * @param decorations The decorations of the string.
     */
    void append(String message, MessageDecoration... decorations);

    /**
     * Appends a mentionable entity (usually a user or channel) to the message.
     *
     * @param entity The entity to mention.
     */
    void append(Mentionable entity);

    /**
     * Appends the string representation of the object (calling {@link String#valueOf(Object)} method) to the message.
     *
     * @param object The object to append.
     * @see StringBuilder#append(Object)
     */
    void append(Object object);

    /**
     * Appends a new line to the message.
     */
    void appendNewLine();

    /**
     * Sets the content of the message.
     * This method overwrites all previous content changes
     * (using {@link #append(String, MessageDecoration...)} for example).
     *
     * @param content The new content of the message.
     */
    void setContent(String content);

    /**
     * Sets the embed of the message.
     *
     * @param embed The embed to set.
     */
    void setEmbed(EmbedBuilder embed);

    /**
     * Sets if the message should be text to speech.
     *
     * @param tts Whether the message should be text to speech or not.
     */
    void setTts(boolean tts);

    /**
     * Adds a file to the message.
     *
     * @param image The image to add as an attachment.
     * @param fileName The file name of the image.
     * @see #addAttachment(BufferedImage, String)
     */
    void addFile(BufferedImage image, String fileName);

    /**
     * Adds a file to the message.
     *
     * @param file The file to add as an attachment.
     * @see #addAttachment(File)
     */
    void addFile(File file);

    /**
     * Adds a file to the message.
     *
     * @param icon The icon to add as an attachment.
     * @see #addAttachment(Icon)
     */
    void addFile(Icon icon);

    /**
     * Adds a file to the message.
     *
     * @param url The url of the attachment.
     * @see #addAttachment(URL)
     */
    void addFile(URL url);

    /**
     * Adds a file to the message.
     *
     * @param bytes The bytes of the file.
     * @param fileName The name of the file.
     * @see #addAttachment(byte[], String)
     */
    void addFile(byte[] bytes, String fileName);

    /**
     * Adds a file to the message.
     *
     * @param stream The stream of the file.
     * @param fileName The name of the file.
     * @see #addAttachment(InputStream, String)
     */
    void addFile(InputStream stream, String fileName);

    /**
     * Adds an attachment to the message.
     *
     * @param image The image to add as an attachment.
     * @param fileName The file name of the image.
     */
    void addAttachment(BufferedImage image, String fileName);

    /**
     * Adds an attachment to the message.
     *
     * @param file The file to add as an attachment.
     */
    void addAttachment(File file);

    /**
     * Adds an attachment to the message.
     *
     * @param icon The icon to add as an attachment.
     */
    void addAttachment(Icon icon);

    /**
     * Adds an attachment to the message.
     *
     * @param url The url of the attachment.
     */
    void addAttachment(URL url);

    /**
     * Adds an attachment to the message.
     *
     * @param bytes The bytes of the file.
     * @param fileName The name of the file.
     */
    void addAttachment(byte[] bytes, String fileName);

    /**
     * Adds an attachment to the message.
     *
     * @param stream The stream of the file.
     * @param fileName The name of the file.
     */
    void addAttachment(InputStream stream, String fileName);

    /**
     * Sets the nonce of the message.
     *
     * @param nonce The nonce to set.
     */
    void setNonce(String nonce);

    /**
     * Gets the {@link StringBuilder} which is used to build the message.
     *
     * @return The StringBuilder which is used to build the message.
     */
    StringBuilder getStringBuilder();

    /**
     * Sends the message.
     *
     * @param user The user to which the message should be sent.
     * @return The sent message.
     */
    CompletableFuture<Message> send(User user);

    /**
     * Sends the message.
     *
     * @param channel The channel in which the message should be sent.
     * @return The sent message.
     */
    CompletableFuture<Message> send(TextChannel channel);

    /**
     * Sends the message.
     *
     * @param messageable The receiver of the message.
     * @return The sent message.
     */
    CompletableFuture<Message> send(Messageable messageable);

}
