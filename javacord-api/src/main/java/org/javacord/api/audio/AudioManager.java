package org.javacord.api.audio;

import org.javacord.api.entity.channel.ServerVoiceChannel;
import org.javacord.api.entity.server.Server;

import java.util.Collection;
import java.util.Optional;

public interface AudioManager {

    Collection<VoiceConnection> getVoiceConnections();

    Optional<VoiceConnection> getVoiceConnection(ServerVoiceChannel channel);

    Optional<VoiceConnection> getVoiceConnection(Server server);

}
