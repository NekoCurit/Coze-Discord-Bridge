package catx.feitu.CozeProxy.Protocol;

import catx.feitu.CozeProxy.Exceptions.InvalidDiscordChannelException;
import catx.feitu.CozeProxy.Protocol.Exception.InvalidUserException;
import catx.feitu.CozeProxy.Protocol.Exception.ProtocolNotLoginException;
import catx.feitu.CozeProxy.Protocol.Exception.ProtocolAPIFailedException;
import catx.feitu.CozeProxy.Protocol.Exception.UnSupportedProtocolException;
import catx.feitu.CozeProxy.Protocol.Listener.SlackListener;
import catx.feitu.CozeProxy.Protocol.Types.UploadFile;
import catx.feitu.CozeProxy.Protocol.Utils.DiscordUtils;
import catx.feitu.DiscordSelfClient.client.SelfClient;
import catx.feitu.DiscordSelfClient.client.Types.DiscordAttachment;
import catx.feitu.DiscordSelfClient.client.impl.Message;
import com.slack.api.Slack;
import com.slack.api.bolt.App;
import com.slack.api.bolt.AppConfig;
import com.slack.api.bolt.socket_mode.SocketModeApp;
import com.slack.api.methods.MethodsClient;
import com.slack.api.methods.response.conversations.ConversationsCreateResponse;
import com.slack.api.methods.response.files.FilesUploadResponse;
import com.slack.api.model.Attachment;
import com.slack.api.model.event.MessageEvent;
import org.javacord.api.entity.channel.ChannelCategory;
import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.user.User;
import org.javacord.api.entity.user.UserStatus;

import java.net.Proxy;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;


public class ProtocolUtil {
    public String apiSelected;
    public SelfClient api_discord;
    public MethodsClient api_slack;
    public App api_slack_listen;
    public UniversalEventListener eventListener;
    public UniversalEventListenerConfig config;
    public ProtocolMessageCode code;

    public void setConfig(UniversalEventListenerConfig config) {
        this.config = config;
    }
    public void login(String protocol ,String token ,Proxy proxy) throws Exception {
        code = new ProtocolMessageCode(protocol);
        apiSelected = protocol;
        switch (protocol){
            case catx.feitu.CozeProxy.Protocol.Protocols.DISCORD:
                api_discord = new SelfClient(token);
                api_discord.setProxy(proxy);
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
                api_discord = null;
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
                        uploadFiles.add(api_discord.upLoadAttachment(file.getByte(),file.getFileName(),channelID));
                    }
                }
                api_discord.sendMessage(message ,channelID ,uploadFiles);

                Thread thread = new Thread(() -> { // Websocket监听器不会写(其实是太耗时了)
                    try {
                        Thread.sleep(1000);
                        int attempt = 0; // 重试次数
                        Message latestMessage = api_discord.getLatestMessage(channelID);
                        if (!latestMessage.getUser().isBot()) { // 如果是bot就已经出现 不需要再等待
                            while (!latestMessage.getUser().isBot()) {
                                if (attempt > 20) { return; }
                                latestMessage = api_discord.getLatestMessage(channelID);
                                try { Thread.sleep(500); } catch (InterruptedException ignored) {}
                                System.out.println(attempt);
                                System.out.println(latestMessage.getId());
                                attempt++;
                            }
                        }
                        eventListener.onStartGenerate(channelID);
                        attempt = 0;
                        while (attempt < 120) {
                            latestMessage = api_discord.getMessage(channelID ,latestMessage.getId());

                            List<String> eventFiles = new CopyOnWriteArrayList<>(); // 存储嵌入附件URL
                            for (catx.feitu.DiscordSelfClient.client.impl.Attachment attachment : latestMessage.getAttachments()) {
                                eventFiles.add(attachment.getUrl());
                            }

                            eventListener.onMessageStream(channelID ,new UniversalMessage()
                                    .setContent(latestMessage.getContent())
                                    .setFiles(eventFiles)
                                    .setHasButton(latestMessage.isHasComponents())
                            );
                            if (latestMessage.isHasComponents()) { return; }
                            try { Thread.sleep(1000); } catch (InterruptedException ignored) {}
                            System.out.println(attempt);
                            attempt++;
                        }
                    } catch (Exception ignored) { }
                });
                thread.start();
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
                return api_discord.createChannel(config.filterServerID ,name ,category);
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
                api_discord.deleteChannel(channelID);
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
                return api_discord.getChannel(channelID).getName();
            case catx.feitu.CozeProxy.Protocol.Protocols.SLACK:
        }
        throw new UnSupportedProtocolException();
    }
    public void setChannelName(String channelID ,String name) throws Exception {
        switch (apiSelected){
            case catx.feitu.CozeProxy.Protocol.Protocols.DISCORD:
                api_discord.renameChannel(channelID ,name);
                return;
            case catx.feitu.CozeProxy.Protocol.Protocols.SLACK:
        }
        throw new UnSupportedProtocolException();
    }
    public boolean isChannelFound(String channelID) throws Exception {
        switch (apiSelected){
            case catx.feitu.CozeProxy.Protocol.Protocols.DISCORD:
                try {
                    api_discord.getChannel(channelID);
                    return true;
                } catch (Exception ignored) { return false;}

            case catx.feitu.CozeProxy.Protocol.Protocols.SLACK:
        }
        throw new UnSupportedProtocolException();
    }
    public boolean isUserOnline(String userID) throws Exception {
        switch (apiSelected){
            case catx.feitu.CozeProxy.Protocol.Protocols.DISCORD:
                return true;

            case catx.feitu.CozeProxy.Protocol.Protocols.SLACK:
        }
        throw new UnSupportedProtocolException();
    }
}
