package catx.feitu.coze_discord_bridge.api;

import catx.feitu.coze_discord_bridge.Config.ConfigManage;
import catx.feitu.coze_discord_bridge.Discord.Discord;
import catx.feitu.coze_discord_bridge.Misc.BotReplyType;
import catx.feitu.coze_discord_bridge.Misc.CacheManager;
import catx.feitu.coze_discord_bridge.Misc.LockManager;
import catx.feitu.coze_discord_bridge.Misc.TempFileManger;
import catx.feitu.coze_discord_bridge.api.Exceptions.*;
import catx.feitu.coze_discord_bridge.api.FunctionalInterface.ChatStreamEvent;
import catx.feitu.coze_discord_bridge.api.Types.ConversationInfo;
import catx.feitu.coze_discord_bridge.api.Types.GenerateMessage;
import org.javacord.api.entity.channel.*;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.server.Server;

import java.io.File;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class CozeGPT {
    private static Server Server = null;
    /**** 对话 ****/
    public static GenerateMessage Chat(String Prompts, String ConversationID, List<File> Files, ChatStreamEvent event) throws Exception {
        if (Objects.equals(Prompts, "")) {
            throw new InvalidPromptException();
        }
        // 取服务器对象
        GetServer();
        // 取对应子频道
        Optional<ServerChannel> channel = Server.getChannelById(CacheManager.Cache_GetName2Channel(ConversationID));
        if (channel.isEmpty()) {
            throw new InvalidConversationException(ConversationID);
        }
        TextChannel textChannel = (TextChannel) channel.get();
        // 锁定 避免同频道多次对话
        LockManager.getLock(textChannel.getIdAsString()).lock();
        // 初始化回复记录
        CacheManager.Cache_BotReplyClear(textChannel.getIdAsString());
        try {
            CompletableFuture<Message> send = null;
            if (Prompts.length() > 2000) { // 长文本发送消息
                if (!ConfigManage.Configs.Disable_2000Limit_Unlock) {
                    throw new PromptTooLongException(Prompts,2000);
                }
                TempFileManger.fwrite_String(Prompts);
                // 仅提及(@)唤醒机器人
                send = textChannel.sendMessage(
                        "<@" + ConfigManage.Configs.CozeBot_id + ">"
                );
                File PromptX = TempFileManger.fwrite_String(Prompts);
                send = send.thenCompose(message -> textChannel.sendMessage(PromptX));
            } else {
                send = textChannel.sendMessage( // 默认发送消息
                        "<@" + ConfigManage.Configs.CozeBot_id + ">" + Prompts
                );
            }
            // 发送附件(图片)处理
            if (Files != null)  {
                for (File file : Files) {
                    send = send.thenCompose(message -> textChannel.sendMessage(file));
                }
            }
            // 发送消息
            Message message = send.join();
            // 在此之下为bot回复消息处理阶段
            boolean BotStartGenerate = false;
            int attempt = 0; // 重试次数
            int maxRetries = 20; // 最大尝试次数
            while (!BotStartGenerate) {
                attempt++;
                if (attempt > maxRetries) {
                    throw new RecvMsgException("超时无回应:未开始生成");
                }
                BotStartGenerate = CacheManager.Cache_BotStartGenerate_Get(textChannel.getIdAsString());
                // 等待200ms
                try { Thread.sleep(200); } catch (InterruptedException ignored) {}
            }
            BotReplyType Reply = new BotReplyType();
            String LatestMessage = "";
            attempt = 0; // 重置重试次数
            maxRetries = 600; // 最大尝试次数
            // 超时 2 分钟
            while (!Reply.Done) {
                attempt++;
                if (attempt > maxRetries) {
                    throw new RecvMsgException("超时无回应:超过设定时间");
                }
                Reply = CacheManager.Cache_BotReplyMessageEx(textChannel.getIdAsString());
                if (!event.handle(Reply.TextMessage, Reply.TextMessage.replace(LatestMessage,""))) {
                    throw new StopGenerateException();
                }
                LatestMessage = Reply.TextMessage;
                try { Thread.sleep(200); } catch (InterruptedException ignored) {}
            }
            GenerateMessage return_info = new GenerateMessage();
            return_info.Message = Reply.TextMessage;
            return_info.Files = Reply.Files;
            return return_info;
        } catch (Exception e) {
            LockManager.getLock(textChannel.getIdAsString()).unlock();
            throw e;
        }
    }
    public static GenerateMessage Chat(String Prompts, String ConversationID, List<File> Files) throws Exception {
        return Chat(Prompts ,ConversationID ,Files ,(ALLGenerateMessages, NewGenerateMessage) -> { return true; });
    }
    public static GenerateMessage Chat(String Prompts, String ConversationID, ChatStreamEvent event) throws Exception {
        return Chat(Prompts ,ConversationID ,null ,event);
    }
    public static GenerateMessage Chat(String Prompts, String ConversationID) throws Exception {
        return Chat(Prompts ,ConversationID ,(ALLGenerateMessages, NewGenerateMessage) -> { return true; });
    }

    /**** 创建聊天 ****/
    public static String CreateConversation (String ConversationName) throws Exception {
        GetServer();
        // 分类处理
        Optional<Channel> Category = Discord.api.getChannelById(ConfigManage.Configs.Discord_CreateChannel_Category);
        ChannelCategory category = (ChannelCategory) Category.orElse(null);
        // 已有对话名称检查
        String ChannelID = CacheManager.Cache_GetName2Channel(ConversationName);
        if (Server.getChannelById(ChannelID).isPresent()) {
            throw new ConversationAlreadyExistsException(ConversationName);
        }
        ServerTextChannel channel = Server.createTextChannelBuilder()
                .setName(ConversationName)
                .setCategory(category)
                .create()
                .join();
        // 写入存储
        CacheManager.Cache_AddName2Channel(channel.getName(),channel.getIdAsString());
        // 返回数据
        return channel.getIdAsString();
    }
    /**** 删除聊天 ****/
    public static void DeleteConversation (String ConversationName) throws Exception {
        GetServer();
        Optional<ServerChannel> channel = Server.getChannelById(CacheManager.Cache_GetName2Channel(ConversationName));
        if (channel.isEmpty()) {
            throw new InvalidConversationException(ConversationName);
        }
        channel.get().delete().join();
    }
    /**** 聊天改名 ****/
    public static String RenameConversation (String OldConversationName, String NewConversationName) throws Exception {
        GetServer();
        Optional<ServerChannel> channel = Server.getChannelById(CacheManager.Cache_GetName2Channel(OldConversationName));
        if (channel.isEmpty()) {
            throw new InvalidConversationException(OldConversationName);
        }
        String ChannelID = CacheManager.Cache_GetName2Channel(NewConversationName);
        if (Server.getChannelById(ChannelID).isPresent()) {
            throw new ConversationAlreadyExistsException(NewConversationName);
        }
        channel.get().updateName(NewConversationName).join();
        CacheManager.Cache_DelName2Channel(OldConversationName);
        CacheManager.Cache_AddName2Channel(NewConversationName,ChannelID);
        return channel.get().getIdAsString();
    }
    /**** 获取对话信息 ****/
    public static ConversationInfo GetConversationInfo (String ConversationName) throws Exception {
        GetServer();
        String ChannelID = CacheManager.Cache_GetName2Channel(ConversationName);
        Optional<ServerChannel> Channel = Server.getChannelById(ChannelID);
        if (Channel.isEmpty()) {
            throw new InvalidConversationException(ConversationName);
        }
        ConversationInfo return_info = new ConversationInfo();
        return_info.Name = Channel.get().getName();
        return_info.ID = Channel.get().getIdAsString();
        return return_info;
    }

    private static void GetServer() throws InvalidConfigException {
        if (Server == null) {
            Optional<Server> optionalServer = Discord.api.getServerById(ConfigManage.Configs.CozeBot_InServer_id);
            if (optionalServer.isEmpty()) {
                throw new InvalidConfigException("CozeBot_InServer_id", "服务器ID不存在");
            }
            Server = optionalServer.get();
        }
    }
}
