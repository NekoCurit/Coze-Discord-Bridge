package catx.feitu.CozeProxy.Protocol.Utils;

import catx.feitu.CozeProxy.Exceptions.InvalidDiscordChannelException;
import catx.feitu.CozeProxy.Exceptions.InvalidDiscordServerException;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.channel.Channel;
import org.javacord.api.entity.channel.ServerChannel;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.server.Server;

import java.util.Optional;

public class DiscordUtils {
    public static Server GetDiscordServer(DiscordApi api , String serverID) throws Exception {
        Optional<Server> optionalServer = api.getServerById(serverID);
        if (optionalServer.isEmpty()) {
            throw new InvalidDiscordServerException();
        }
        return optionalServer.get();
    }
    public static TextChannel GetDiscordChannelAsTextChannel(Server server , String channelID) throws Exception {
        Channel Channel = GetDiscordChannel(server ,channelID);
        if (!(Channel instanceof TextChannel)) {
            throw new InvalidDiscordChannelException(channelID);
        }
        return (TextChannel) Channel;
    }
    public static ServerChannel GetDiscordChannelAsServerChannel(Server server , String channelID) throws Exception {
        Channel Channel = GetDiscordChannel(server ,channelID);
        if (!(Channel instanceof ServerChannel)) {
            throw new InvalidDiscordChannelException(channelID);
        }
        return (ServerChannel) Channel;
    }
    public static Channel GetDiscordChannel(Server server , String channelID) throws Exception {
        Optional<ServerChannel> Channel = server.getChannelById(channelID);
        if (Channel.isEmpty()) {
            throw new InvalidDiscordChannelException(channelID);
        }
        return Channel.get();
    }
}
