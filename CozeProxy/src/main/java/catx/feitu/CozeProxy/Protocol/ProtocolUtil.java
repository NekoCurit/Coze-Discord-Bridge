package catx.feitu.CozeProxy.Protocol;

import catx.feitu.CozeProxy.Exceptions.InvalidDiscordChannelException;
import catx.feitu.CozeProxy.Protocol.Exception.InvalidUserException;
import catx.feitu.CozeProxy.Protocol.Exception.ProtocolNotLoginException;
import catx.feitu.CozeProxy.Protocol.Exception.UnSupportedProtocolException;
import catx.feitu.CozeProxy.Protocol.Listener.DiscordListener;
import catx.feitu.CozeProxy.Protocol.Listener.SlackListener;
import catx.feitu.CozeProxy.Protocol.Utils.DiscordUtils;
import com.slack.api.bolt.App;
import com.slack.api.bolt.AppConfig;
import com.slack.api.bolt.socket_mode.SocketModeApp;
import com.slack.api.model.event.MessageEvent;
import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.entity.channel.ChannelCategory;
import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.intent.Intent;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.user.User;
import org.javacord.api.entity.user.UserStatus;

import java.io.InputStream;
import java.net.Proxy;
import java.util.List;
import java.util.Optional;

public class ProtocolUtil {
    public String apiSelected;
    public DiscordApi api_discord;
    public App api_slack;
    public UniversalEventListener eventListener;
    public UniversalEventListenerConfig config;

    public void setEventListener(UniversalEventListener event) {
        eventListener = event;
    }
    public void setConfig(UniversalEventListenerConfig config) {
        this.config = config;
    }
    public void login(String protocol ,String token ,Proxy proxy) throws Exception {
        switch (protocol){
            case catx.feitu.CozeProxy.Protocol.Protocols.DISCORD:
                api_discord.addListener(new DiscordListener(eventListener ,config));
                api_discord = new DiscordApiBuilder()
                        .setToken(token)
                        .addIntents(Intent.MESSAGE_CONTENT)
                        .setProxy(proxy)
                        .login()
                        .join();
            case catx.feitu.CozeProxy.Protocol.Protocols.SLACK:
                AppConfig config = new AppConfig();
                config.setSingleTeamBotToken(token);
                api_slack = new App(config);
                api_slack.event(MessageEvent.class, new SlackListener(eventListener ,this.config));
                new SocketModeApp(api_slack).start();

        }
    }
    public void disconnect() throws Exception {
        switch (apiSelected){
            case catx.feitu.CozeProxy.Protocol.Protocols.DISCORD:
                if (api_discord == null) {
                    throw new ProtocolNotLoginException();
                }
                api_discord.disconnect();
                api_discord= null;
                return;
            case catx.feitu.CozeProxy.Protocol.Protocols.SLACK:
                new SocketModeApp(api_slack).stop();
                api_slack = null;
                return;

        }
        throw new UnSupportedProtocolException();
    }
    public void sendMessage(String channelID , String message , List<InputStream> files) throws Exception {
        switch (apiSelected){
            case catx.feitu.CozeProxy.Protocol.Protocols.DISCORD:
                MessageBuilder messageBuilder = new MessageBuilder()
                        .append(message);
                // 发送附件(图片)处理
                if (files != null)  {
                    for (InputStream file : files) {
                        messageBuilder.addAttachment(file ,"file");
                    }
                }
                messageBuilder.send(DiscordUtils.GetDiscordChannelAsTextChannel(
                        DiscordUtils.GetDiscordServer(api_discord ,config.filterServerID) ,channelID)
                );
                return;
            case catx.feitu.CozeProxy.Protocol.Protocols.SLACK:

        }
        throw new UnSupportedProtocolException();
    }
    public String createChannel(String name ,String category) throws Exception {
        switch (apiSelected){
            case catx.feitu.CozeProxy.Protocol.Protocols.DISCORD:
                ChannelCategory categoryObj = (ChannelCategory) DiscordUtils.GetDiscordChannel(DiscordUtils.GetDiscordServer(api_discord,config.filterServerID),category);
                // 创建
                ServerTextChannel channel = DiscordUtils.GetDiscordServer(api_discord,config.filterServerID)
                        .createTextChannelBuilder()
                        .setName("default")
                        .setCategory(categoryObj)
                        .create()
                        .join();
                // 返回数据
                return channel.getIdAsString();
            case catx.feitu.CozeProxy.Protocol.Protocols.SLACK:
        }
        throw new UnSupportedProtocolException();
    }
    public void deleteChannel(String channelID) throws Exception {
        switch (apiSelected){
            case catx.feitu.CozeProxy.Protocol.Protocols.DISCORD:
                DiscordUtils.GetDiscordChannelAsServerChannel(DiscordUtils.GetDiscordServer(api_discord,config.filterServerID),
                        channelID).delete().join();
                return;
            case catx.feitu.CozeProxy.Protocol.Protocols.SLACK:
        }
        throw new UnSupportedProtocolException();
    }
    public String getChannelName(String channelID) throws Exception {
        switch (apiSelected){
            case catx.feitu.CozeProxy.Protocol.Protocols.DISCORD:
                return DiscordUtils.GetDiscordChannelAsServerChannel(DiscordUtils.GetDiscordServer(api_discord,config.filterServerID),
                        channelID).getName();
            case catx.feitu.CozeProxy.Protocol.Protocols.SLACK:
        }
        throw new UnSupportedProtocolException();
    }
    public void setChannelName(String channelID ,String name) throws Exception {
        switch (apiSelected){
            case catx.feitu.CozeProxy.Protocol.Protocols.DISCORD:
                DiscordUtils.GetDiscordChannelAsServerChannel(DiscordUtils.GetDiscordServer(api_discord,config.filterServerID),
                        channelID).updateName(name).join();
                return;
            case catx.feitu.CozeProxy.Protocol.Protocols.SLACK:
        }
        throw new UnSupportedProtocolException();
    }
    public boolean isChannelFound(String channelID) throws Exception {
        switch (apiSelected){
            case catx.feitu.CozeProxy.Protocol.Protocols.DISCORD:
                try {
                    DiscordUtils.GetDiscordChannelAsServerChannel(DiscordUtils.GetDiscordServer(api_discord,config.filterServerID),
                            channelID).getName();
                    return true;
                } catch (InvalidDiscordChannelException ignored) {
                    return false;
                }
            case catx.feitu.CozeProxy.Protocol.Protocols.SLACK:
        }
        throw new UnSupportedProtocolException();
    }
    public boolean isUserOnline(String userID) throws Exception {
        switch (apiSelected){
            case catx.feitu.CozeProxy.Protocol.Protocols.DISCORD:
                Optional<User> user = DiscordUtils.GetDiscordServer(api_discord,config.filterServerID)
                        .getMemberById(userID);
                if (user.isEmpty()) {
                    throw new InvalidUserException(userID);
                }
                return user.get().getStatus() != UserStatus.OFFLINE;
            case catx.feitu.CozeProxy.Protocol.Protocols.SLACK:
        }
        throw new UnSupportedProtocolException();
    }
}
