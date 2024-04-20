package catx.feitu.CozeProxy.protocol;

import catx.feitu.CozeProxy.protocol.exception.ProtocolNotLoginException;
import catx.feitu.CozeProxy.protocol.exception.UnSupportedProtocolException;
import catx.feitu.CozeProxy.protocol.listene.EventListene;
import catx.feitu.CozeProxy.protocol.listene.EventListeneConfig;
import catx.feitu.CozeProxy.protocol.listene.api.DiscordListener;
import catx.feitu.CozeProxy.protocol.impl.UploadFile;
import catx.feitu.CozeProxy.protocol.message.MessageCode;
import catx.feitu.DiscordSelfClient.client.SelfClient;
import catx.feitu.DiscordSelfClient.client.Types.DiscordAttachment;

import java.net.Proxy;
import java.util.ArrayList;
import java.util.List;


public class ProtocolHandle {
    public String apiSelected;
    public SelfClient api_discord;
    public EventListene eventListene;
    public EventListeneConfig config;
    public MessageCode code;

    public void setConfig(EventListeneConfig config) {
        this.config = config;
    }
    public void login(String protocol ,String token ,Proxy proxy) throws Exception {
        code = new MessageCode(protocol);
        apiSelected = protocol;
        switch (protocol){
            case catx.feitu.CozeProxy.protocol.Protocols.DISCORD:
                api_discord = new SelfClient(token);
                api_discord.setProxy(proxy);

                config.filterSelfUserID = api_discord.getSelf().id();
                return;
        }
        throw new UnSupportedProtocolException();
    }
    public void disconnect() throws Exception {
        switch (apiSelected){
            case catx.feitu.CozeProxy.protocol.Protocols.DISCORD:
                if (api_discord == null) {
                    throw new ProtocolNotLoginException();
                }
                api_discord = null;
                return;
        }
        throw new UnSupportedProtocolException();
    }
    public void sendMessage(String channelID , String message , List<UploadFile> files) throws Exception {
        switch (apiSelected){
            case catx.feitu.CozeProxy.protocol.Protocols.DISCORD:
                List<DiscordAttachment> uploadFiles = new ArrayList<>();
                if (files != null)  {
                    for (UploadFile file : files) {
                        uploadFiles.add(api_discord.upLoadAttachment(file.getByte(),file.getFileName(),channelID));
                    }
                }
                api_discord.sendMessage(message ,channelID ,uploadFiles);
                new DiscordListener().listen(api_discord ,channelID , eventListene,config);
                return;

        }
        throw new UnSupportedProtocolException();
    }
    public String createChannel(String name ,String category) throws Exception {
        String realName = name == null ? "default" : name;
        switch (apiSelected){
            case catx.feitu.CozeProxy.protocol.Protocols.DISCORD:
                return api_discord.createChannel(config.filterServerID ,name ,category);
        }
        throw new UnSupportedProtocolException();
    }
    public void inviteBotInChannel(String channelID ,String botID) throws Exception {
        switch (apiSelected){
            case catx.feitu.CozeProxy.protocol.Protocols.DISCORD:
                return;
        }
        throw new UnSupportedProtocolException();
    }
    public void deleteChannel(String channelID) throws Exception {
        switch (apiSelected){
            case catx.feitu.CozeProxy.protocol.Protocols.DISCORD:
                api_discord.deleteChannel(channelID);
                return;
        }
        throw new UnSupportedProtocolException();
    }
    public String getChannelName(String channelID) throws Exception {
        switch (apiSelected){
            case catx.feitu.CozeProxy.protocol.Protocols.DISCORD:
                return api_discord.getChannel(channelID).getName();
        }
        throw new UnSupportedProtocolException();
    }
    public void setChannelName(String channelID ,String name) throws Exception {
        switch (apiSelected){
            case catx.feitu.CozeProxy.protocol.Protocols.DISCORD:
                api_discord.renameChannel(channelID ,name);
                return;
        }
        throw new UnSupportedProtocolException();
    }
    public boolean isChannelFound(String channelID) throws Exception {
        switch (apiSelected){
            case catx.feitu.CozeProxy.protocol.Protocols.DISCORD:
                try {
                    api_discord.getChannel(channelID);
                    return true;
                } catch (Exception ignored) { return false;}
        }
        throw new UnSupportedProtocolException();
    }
    public boolean isUserOnline(String userID) throws Exception {
        switch (apiSelected){
            case catx.feitu.CozeProxy.protocol.Protocols.DISCORD:
                return true;
        }
        throw new UnSupportedProtocolException();
    }
}
