package catx.feitu.CozeProxy.Protocol;

import catx.feitu.CozeProxy.Exceptions.InvalidDiscordChannelException;
import catx.feitu.CozeProxy.Protocol.Exception.InvalidUserException;
import catx.feitu.CozeProxy.Protocol.Exception.ProtocolNotLoginException;
import catx.feitu.CozeProxy.Protocol.Exception.ProtocolAPIFailedException;
import catx.feitu.CozeProxy.Protocol.Exception.UnSupportedProtocolException;
import catx.feitu.CozeProxy.Protocol.Listener.DiscordListener;
import catx.feitu.CozeProxy.Protocol.Listener.SlackListener;
import catx.feitu.CozeProxy.Protocol.Types.UploadFile;
import catx.feitu.CozeProxy.Protocol.Utils.DiscordUtils;
import catx.feitu.DiscordSelfClient.client.SelfClient;
import catx.feitu.DiscordSelfClient.client.Types.DiscordAttachment;
import com.slack.api.Slack;
import com.slack.api.bolt.App;
import com.slack.api.bolt.AppConfig;
import com.slack.api.bolt.socket_mode.SocketModeApp;
import com.slack.api.methods.MethodsClient;
import com.slack.api.methods.response.conversations.ConversationsCreateResponse;
import com.slack.api.methods.response.files.FilesUploadResponse;
import com.slack.api.model.Attachment;
import com.slack.api.model.event.MessageEvent;
import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.entity.channel.ChannelCategory;
import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.intent.Intent;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.user.User;
import org.javacord.api.entity.user.UserStatus;

import java.net.Proxy;
import java.util.*;
import java.util.stream.Collectors;

public class ProtocolUtil {
    public String apiSelected;
    public DiscordApi api_discord;
    public SelfClient api_discord2;
    public MethodsClient api_slack;
    public App api_slack_listen;
    public UniversalEventListener eventListener;
    public UniversalEventListenerConfig config;
    public ProtocolMessageCode code;

    public void setConfig(UniversalEventListenerConfig config) {
        this.config = config;
    }
    public void login(String protocol ,String token ,Proxy proxy,String token2) throws Exception {
        code = new ProtocolMessageCode(protocol);
        apiSelected = protocol;
        switch (protocol){
            case catx.feitu.CozeProxy.Protocol.Protocols.DISCORD:
                api_discord2 = new SelfClient(token2);
                api_discord2.setProxy(proxy);
                api_discord = new DiscordApiBuilder()
                        .setToken(token)
                        .addIntents(Intent.MESSAGE_CONTENT)
                        .setProxy(proxy)
                        .login()
                        .join();
                api_discord.addListener(new DiscordListener(eventListener ,config));
                return;
            case catx.feitu.CozeProxy.Protocol.Protocols.SLACK:
                AppConfig config = new AppConfig();
                config.setSingleTeamBotToken(token);
                api_slack = new Slack().methods(token);

                api_slack_listen = new App(config);
                api_slack_listen.event(MessageEvent.class, new SlackListener(eventListener ,this.config));
                new SocketModeApp(api_slack_listen).start();
                return;
        }
        throw new UnSupportedProtocolException();
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
                new SocketModeApp(api_slack_listen).stop();
                api_slack_listen = null;
                api_slack = null;
                return;

        }
        throw new UnSupportedProtocolException();
    }
    public void sendMessage(String channelID , String message , List<UploadFile> files) throws Exception {
        switch (apiSelected){
            case catx.feitu.CozeProxy.Protocol.Protocols.DISCORD:
                List<DiscordAttachment> uploadFiles = new ArrayList<>();
                if (files != null)  {
                    for (UploadFile file : files) {
                        uploadFiles.add(api_discord2.upLoadAttachment(file.getByte(),file.getFileName(),channelID));
                    }
                }
                api_discord2.sendMessage(message ,channelID ,uploadFiles);
                /*
                MessageBuilder messageBuilder = new MessageBuilder()
                        .append(message);
                // 发送附件(图片)处理
                if (files != null)  {
                    for (UploadFile file : files) {
                        messageBuilder.addAttachment(file.getByteArrayInputStream() ,file.getFileName());
                    }
                }
                messageBuilder.send(DiscordUtils.GetDiscordChannelAsTextChannel(
                        DiscordUtils.GetDiscordServer(api_discord ,config.filterServerID) ,channelID)
                );
                */
                return;
            case catx.feitu.CozeProxy.Protocol.Protocols.SLACK:
                // by ChatGPT
                List<String> fileIds = new ArrayList<>();

                // 先上传文件
                if (files != null && !files.isEmpty()) {
                    for (UploadFile file : files) {
                        FilesUploadResponse uploadResponse = api_slack.filesUpload(req -> req
                                .channels(Collections.singletonList(channelID))
                                .fileData(file.getByte())
                                .filename(file.getFileName())
                                .title(file.getFileName())
                        );
                        if (uploadResponse.isOk()) {
                            fileIds.add(uploadResponse.getFile().getId()); // 每个上传的文件的ID都加入列表
                        } else {
                            //处理上传错误情况
                            throw new ProtocolAPIFailedException();
                        }
                    }
                    if (!api_slack.chatPostMessage(req -> req
                            .channel(channelID)
                            .text(message)
                            .attachments(fileIds.stream().map(fileId -> {
                                Attachment attachment = new Attachment();
                                attachment.setFallback("The file could not be displayed.");
                                // attachment 中添加 file ID
                                attachment.setFooter("<file://" + fileId + "|Open the file>");
                                return attachment;
                            }).collect(Collectors.toList()))).isOk()
                    ) {
                        throw new ProtocolAPIFailedException();
                    }
                }
                return;
        }
        throw new UnSupportedProtocolException();
    }
    public String createChannel(String name ,String category) throws Exception {
        String realName = name == null ? "default" : name;
        switch (apiSelected){
            case catx.feitu.CozeProxy.Protocol.Protocols.DISCORD:
                ChannelCategory categoryObj = Objects.equals(category, "") || category == null ?
                        null :
                        (ChannelCategory) DiscordUtils.GetDiscordChannel(DiscordUtils.GetDiscordServer(api_discord,config.filterServerID),category);
                // 创建
                ServerTextChannel channel = DiscordUtils.GetDiscordServer(api_discord,config.filterServerID)
                        .createTextChannelBuilder()
                        .setName(realName)
                        .setCategory(categoryObj)
                        .create()
                        .join();
                // 返回数据
                return channel.getIdAsString();
            case catx.feitu.CozeProxy.Protocol.Protocols.SLACK:
                ConversationsCreateResponse response = api_slack.conversationsCreate(req -> req
                        .name(realName) // 设置你想要创建的频道名
                        .isPrivate(false) // 设置频道是否为私有
                );
                if (!response.isOk()) {
                    throw new ProtocolAPIFailedException();
                }
                return response.getChannel().getId();
        }
        throw new UnSupportedProtocolException();
    }
    public void inviteBotInChannel(String channelID ,String botID) throws Exception {
        switch (apiSelected){
            case catx.feitu.CozeProxy.Protocol.Protocols.DISCORD:
                return;
            case catx.feitu.CozeProxy.Protocol.Protocols.SLACK:
                if (!api_slack.conversationsInvite(req -> req
                    .channel(channelID)
                    .users(Collections.singletonList(botID))     // 需要提供一个或多个用户的 ID
                ).isOk()) {
                    throw new ProtocolAPIFailedException();
                }
                return;
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
                // 尝试删除 如果失败则bot退出
                if (api_slack.adminConversationsDelete(req -> req.channelId(channelID)).isOk()) return;
                if (api_slack.conversationsLeave(req -> req.channel(channelID)).isOk()) return;
                throw new ProtocolAPIFailedException();
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
