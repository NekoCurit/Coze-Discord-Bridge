package catx.feitu.coze_discord_bridge.api;

import catx.feitu.coze_discord_bridge.api.ConversationManage.ConversationData;
import catx.feitu.coze_discord_bridge.api.Exceptions.*;
import catx.feitu.coze_discord_bridge.api.FunctionalInterface.ChatStreamEvent;
import catx.feitu.coze_discord_bridge.api.Listen.MessageListener;
import catx.feitu.coze_discord_bridge.api.LockManage.LockManage;
import catx.feitu.coze_discord_bridge.api.MessageManage.BotGenerateStatusManage;
import catx.feitu.coze_discord_bridge.api.MessageManage.BotResponseManage;
import catx.feitu.coze_discord_bridge.api.MessageManage.BotResponseType;
import catx.feitu.coze_discord_bridge.api.Types.ConversationInfo;
import catx.feitu.coze_discord_bridge.api.Types.GPTFile;
import catx.feitu.coze_discord_bridge.api.Types.GenerateMessage;

import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.entity.channel.*;
import org.javacord.api.entity.intent.Intent;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.server.Server;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class CozeGPT {
    private Server server = null;
    private final CozeGPTConfig config;

    public BotResponseManage BotResponseManage = new BotResponseManage();
    private final BotGenerateStatusManage BotGenerateStatusManage = new BotGenerateStatusManage();
    private final LockManage LockManage = new LockManage();
    public ConversationData conversations = new ConversationData();
    public DiscordApi discord_api;


    public CozeGPT(CozeGPTConfig config) {
        this.config = config;
    }
    public CozeGPT(CozeGPTConfig config,boolean autoLogin) throws Exception {
        this.config = config;
        if (autoLogin) {
            this.Login();
        }
    }
    /**
     * Discord登录
     *
     * @throws Exception       遇到错误会抛出异常
     */
    public void Login() throws Exception {
        if (discord_api != null) {
            discord_api.disconnect();
        }
        discord_api = new DiscordApiBuilder()
                .setToken(config.Discord_Bot_Token)
                .addIntents(Intent.MESSAGE_CONTENT)
                .setProxy(config.Proxy)
                .login()
                .join();
        discord_api.addListener(new MessageListener(this.BotResponseManage ,this.BotGenerateStatusManage ,this.config));
    }
    /**
     * Discord登出
     *
     * @throws Exception       如果Bot未登录,可能会抛出 BotNotLoginException 异常
     */
    public void Logout() throws Exception {
        if (discord_api == null) {
            throw new BotNotLoginException();
        }
        discord_api.disconnect();
        discord_api = null;
    }
    /**
     * 获取标记
     */
    public String getMark() {
        return config.Mark;
    }
    /**
     * 设置标记
     */
    public void setMark(String mark) {
        config.Mark = mark;
    }
    /**
     * 发送一条消息到对话列表并等待Bot回复
     *
     * @param Prompts          提示词,即用户发送的消息,可以填入url上传附件.
     * @param ConversationID   对话ID,可通过 CreateConversation(String name); 获取,必须提供,用于储存上下文信息.
     * @param Files            上传本地文件,可为多个.
     * @param event            填入以支持消息流返回,返回 true 继续生成,返回 false 停止生成.
     * @return                 生成的消息信息.
     * @throws Exception       如果消息生成过程遇到任何问题,则抛出异常.
     */
    public GenerateMessage Chat(String Prompts, String ConversationID, List<GPTFile> Files, ChatStreamEvent event) throws Exception {
        if (Objects.equals(Prompts, "")) {
            throw new InvalidPromptException();
        }
        // 取服务器对象
        GetServer();
        // 取对应子频道
        Optional<ServerChannel> channel = server.getChannelById(conversations.get(ConversationID));
        if (channel.isEmpty()) {
            throw new InvalidConversationException(ConversationID);
        }
        TextChannel textChannel = (TextChannel) channel.get();
        // 锁定 避免同频道多次对话
        LockManage.getLock(textChannel.getIdAsString()).lock();
        // 初始化回复记录
        this.BotGenerateStatusManage.clearGenerateStatus(textChannel.getIdAsString());
        this.BotResponseManage.clearMsg(textChannel.getIdAsString());
        // 开始
        try {
            CompletableFuture<Message> send = null;
            if (Prompts.length() > 2000) { // 长文本发送消息
                if (!config.Disable_2000Limit_Unlock) {
                    throw new PromptTooLongException(Prompts,2000);
                }
                // 仅提及(@)唤醒机器人
                send = textChannel.sendMessage(
                        "<@" + config.CozeBot_id + ">"
                );
                send = send.thenCompose(message -> textChannel.sendMessage( new ByteArrayInputStream(Prompts.getBytes()),"Prompt.txt"));
            } else {
                send = textChannel.sendMessage( // 默认发送消息
                        "<@" + config.CozeBot_id + ">" + Prompts
                );
            }
            // 发送附件(图片)处理
            if (Files != null)  {
                for (GPTFile file : Files) {
                    send = send.thenCompose(message -> textChannel.sendMessage(file.GetByteArrayInputStream(),file.GetFileName()));
                }
            }
            // 发送消息
            send.join();
            // 在此之下为bot回复消息处理阶段 -> 5s 超时
            boolean BotStartGenerate = false;
            int attempt = 0; // 重试次数
            int maxRetries = 25; // 最大尝试次数
            while (!BotStartGenerate) {
                attempt++;
                if (attempt > maxRetries) {
                    throw new RecvMsgException("超时无回应:未开始生成");
                }
                BotStartGenerate = this.BotGenerateStatusManage.getGenerateStatus(textChannel.getIdAsString());
                // 等待200ms
                try { Thread.sleep(200); } catch (InterruptedException ignored) {}
            }
            BotResponseType Response = new BotResponseType();
            String LatestMessage = "";
            attempt = 0; // 重置重试次数
            maxRetries = 120; // 最大尝试次数
            // 超时 2 分钟
            while (!Response.IsCompleted(config.generate_timeout)) {
                attempt++;
                if (attempt > maxRetries) {
                    throw new RecvMsgException("超时无回应:超过设定时间");
                }
                try {
                    Response = this.BotResponseManage.getMsg(textChannel.getIdAsString());
                } catch (NullPointerException ignored) {}
                if (!event.handle(Response.prompt, Response.prompt.replace(LatestMessage,""))) {
                    throw new StopGenerateException();
                }
                LatestMessage = Response.prompt;
                try { Thread.sleep(500); } catch (InterruptedException ignored) {}
            }
            GenerateMessage return_info = new GenerateMessage();
            return_info.Message = Response.prompt;
            return_info.Files = Response.files;
            LockManage.getLock(textChannel.getIdAsString()).unlock();
            return return_info;
        } catch (Exception e) {
            LockManage.getLock(textChannel.getIdAsString()).unlock();
            throw e;
        }
    }
    /**
     * 发送一条消息到对话列表并等待Bot回复
     *
     * @param Prompts          提示词,即用户发送的消息,可以填入url上传附件.
     * @param ConversationID   对话ID,可通过 CreateConversation(String name); 获取,必须提供,用于储存上下文信息.
     * @param Files            上传本地文件,可为多个.
     * @return                 生成的消息信息.
     * @throws Exception       如果消息生成过程遇到任何问题,则抛出异常.
     */
    public GenerateMessage Chat(String Prompts, String ConversationID, List<GPTFile> Files) throws Exception {
        return Chat(Prompts ,ConversationID ,Files ,(ALLGenerateMessages, NewGenerateMessage) -> { return true; });
    }
    /**
     * 发送一条消息到对话列表并等待Bot回复
     *
     * @param Prompts          提示词,即用户发送的消息,可以填入url上传附件.
     * @param ConversationID   对话ID,可通过 CreateConversation(String name); 获取,必须提供,用于储存上下文信息.
     * @param event            填入以支持消息流返回,返回 true 继续生成,返回 false 停止生成.
     * @return                 生成的消息信息.
     * @throws Exception       如果消息生成过程遇到任何问题,则抛出异常.
     */
    public GenerateMessage Chat(String Prompts, String ConversationID, ChatStreamEvent event) throws Exception {
        return Chat(Prompts ,ConversationID ,null ,event);
    }
    /**
     * 发送一条消息到对话列表并等待Bot回复
     *
     * @param Prompts          提示词,即用户发送的消息,可以填入url上传附件.
     * @param ConversationID   对话ID,可通过 CreateConversation(String name); 获取,必须提供,用于储存上下文信息.
     * @return                 生成的消息信息.
     * @throws Exception       如果消息生成过程遇到任何问题,则抛出异常.
     */
    public GenerateMessage Chat(String Prompts, String ConversationID) throws Exception {
        return Chat(Prompts ,ConversationID ,(ALLGenerateMessages, NewGenerateMessage) -> { return true; });
    }
    /**
     * 创建新的对话列表
     *
     * @param ConversationName 对话名词,如果没有关闭对话名词索引功能则后续可以通过对话名词调用对话
     * @return                 对话ID,一段数字,后续可通过此ID调用
     * @throws Exception       如果遇到任何问题,则抛出异常.
     */
    public String CreateConversation (String ConversationName) throws Exception {
        GetServer();
        // 分类处理
        Optional<Channel> Category = discord_api.getChannelById(config.Discord_CreateChannel_Category);
        ChannelCategory category = (ChannelCategory) Category.orElse(null);
        // 已有对话名称检查
        String ChannelID = conversations.get(ConversationName);
        if (server.getChannelById(ChannelID).isPresent()) {
            throw new ConversationAlreadyExistsException(ConversationName);
        }
        ServerTextChannel channel = server.createTextChannelBuilder()
                .setName(ConversationName)
                .setCategory(category)
                .create()
                .join();
        // 写入存储
        conversations.put(channel.getName(),channel.getIdAsString());
        // 返回数据
        return channel.getIdAsString();
    }
    /**
     * 创建新的对话列表
     *
     * @return                 对话ID,一段数字,后续可通过此ID调用
     * @throws Exception       如果遇到任何问题,则抛出异常.
     */
    public String CreateConversation () throws Exception {
        GetServer();
        // 分类处理
        Optional<Channel> Category = discord_api.getChannelById(config.Discord_CreateChannel_Category);
        ChannelCategory category = (ChannelCategory) Category.orElse(null);
        // 创建
        ServerTextChannel channel = server.createTextChannelBuilder()
                .setName("default")
                .setCategory(category)
                .create()
                .join();
        // 返回数据
        return channel.getIdAsString();
    }
    /**
     * 删除对话列表
     *
     * @param ConversationName 对话名词/对话ID
     * @throws Exception       如果遇到任何问题,则抛出异常.
     */
    public void DeleteConversation (String ConversationName) throws Exception {
        GetServer();
        Optional<ServerChannel> channel = server.getChannelById(conversations.get(ConversationName));
        if (channel.isEmpty()) {
            throw new InvalidConversationException(ConversationName);
        }
        channel.get().delete().join();
        conversations.remove(ConversationName);
    }
    /**
     * 修改某个对话的名词
     * 注:对话ID无法修改
     *
     * @param OldConversationName 旧的对话名词/对话ID
     * @param NewConversationName 新的对话名词
     * @throws Exception          如果遇到任何问题,则抛出异常.
     */
    public String RenameConversation (String OldConversationName, String NewConversationName) throws Exception {
        GetServer();
        Optional<ServerChannel> channel = server.getChannelById(conversations.get(OldConversationName));
        if (channel.isEmpty()) {
            throw new InvalidConversationException(OldConversationName);
        }
        String ChannelID = conversations.get(NewConversationName);
        if (server.getChannelById(ChannelID).isPresent()) {
            throw new ConversationAlreadyExistsException(NewConversationName);
        }
        channel.get().updateName(NewConversationName).join();
        conversations.remove(OldConversationName);
        conversations.put(NewConversationName,ChannelID);
        return channel.get().getIdAsString();
    }
    /**
     * 修改某个对话的名词
     * 注:对话ID无法修改
     *
     * @param ConversationName 对话名词/对话ID
     * @return                 对话信息
     * @throws Exception       如果遇到任何问题,则抛出异常.
     */
    public ConversationInfo GetConversationInfo (String ConversationName) throws Exception {
        GetServer();
        String ChannelID = conversations.get(ConversationName);
        Optional<ServerChannel> Channel = server.getChannelById(ChannelID);
        if (Channel.isEmpty()) {
            throw new InvalidConversationException(ConversationName);
        }
        ConversationInfo return_info = new ConversationInfo();
        return_info.Name = Channel.get().getName();
        return_info.ID = Channel.get().getIdAsString();
        return return_info;
    }

    private void GetServer() throws Exception {
        if (discord_api == null) {
            throw new BotNotLoginException();
        }
        if (server == null) {
            Optional<Server> optionalServer = discord_api.getServerById(config.Server_id);
            if (optionalServer.isEmpty()) {
                throw new InvalidDiscordServerException();
            }
            server = optionalServer.get();
        }
    }
}
