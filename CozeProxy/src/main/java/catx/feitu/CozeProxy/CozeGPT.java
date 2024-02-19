package catx.feitu.CozeProxy;

import catx.feitu.CozeProxy.ConversationManage.ConversationData;
import catx.feitu.CozeProxy.Exceptions.ConversationAlreadyExistsException;
import catx.feitu.CozeProxy.Protocol.ProtocolUtil;
import catx.feitu.CozeProxy.Types.ConversationInfo;
import catx.feitu.coze_discord_bridge.api.Exceptions.*;
import catx.feitu.CozeProxy.FunctionalInterface.ChatStreamEvent;
import catx.feitu.coze_discord_bridge.api.Listen.MessageListener;
import catx.feitu.coze_discord_bridge.api.LockManage.LockManage;
import catx.feitu.CozeProxy.MessageManage.BotGenerateStatusManage;
import catx.feitu.CozeProxy.MessageManage.BotResponseManage;
import catx.feitu.CozeProxy.MessageManage.BotResponseType;
import catx.feitu.coze_discord_bridge.api.Types.ConversationInfo;
import catx.feitu.coze_discord_bridge.api.Types.GPTFile;
import catx.feitu.coze_discord_bridge.api.Types.GenerateMessage;

import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.entity.channel.*;
import org.javacord.api.entity.intent.Intent;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.javacord.api.entity.user.UserStatus;

import java.io.ByteArrayInputStream;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class CozeGPT {
    public Server server = null;
    private final CozeGPTConfig config;

    public BotResponseManage BotResponseManage = new BotResponseManage();
    private final BotGenerateStatusManage BotGenerateStatusManage = new BotGenerateStatusManage();
    private final LockManage LockManage = new LockManage();
    private MessageListener MessageListener;
    public ConversationData conversations = new ConversationData();
    public ProtocolUtil protocol;


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
     */
    public void Login() throws Exception {
        protocol.setEventListener();
        protocol.login(config.loginApp ,config.token ,config.Proxy);
    }
    /**
     * Discord登出
     *
     * @throws Exception       如果Bot未登录,可能会抛出 BotNotLoginException 异常
     */
    public void Logout() throws Exception {
        protocol.disconnect();
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
        private_getServer();
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
     * @param conversationName 对话名词,如果没有关闭对话名词索引功能则后续可以通过对话名词调用对话
     * @return                 对话ID,一段数字,后续可通过此ID调用
     * @throws Exception       如果遇到任何问题,则抛出异常.
     */
    public String createConversation (String conversationName) throws Exception {
        // 已有对话名称检查
        if (protocol.isChannelFound(conversations.get(conversationName))) {
            throw new ConversationAlreadyExistsException(conversationName);
        }
        String conversationID = protocol.createChannel(conversationName ,config.Discord_CreateChannel_Category)
        // 写入存储
        conversations.put(conversationName ,conversationID);
        // 返回数据
        return conversationID;
    }
    /**
     * 创建新的对话列表
     *
     * @return                 对话ID,一段数字,后续可通过此ID调用
     * @throws Exception       如果遇到任何问题,则抛出异常.
     */
    public String createConversation () throws Exception {
         return CreateConversation(null);
    }
    /**
     * 删除对话列表
     *
     * @param conversationName 对话名词/对话ID
     * @throws Exception       如果遇到任何问题,则抛出异常.
     */
    public void deleteConversation (String conversationName) throws Exception {
        protocol.deleteChannel(conversations.get(conversationName));
        conversations.remove(conversationName);
    }
    /**
     * 修改某个对话的名词
     * 注:对话ID无法修改
     *
     * @param oldConversationName 旧的对话名词/对话ID
     * @param newConversationName 新的对话名词
     * @throws Exception          如果遇到任何问题,则抛出异常.
     */
    public String renameConversation (String oldConversationName, String newConversationName) throws Exception {
        String conversationID = conversations.get(oldConversationName);
        protocol.setChannelName(conversationID ,newConversationName);
        conversations.put(newConversationName ,conversationID);
        conversations.remove(oldConversationName);
        return conversationID;
    }
    /**
     * 修改某个对话的名词
     * 注:对话ID无法修改
     *
     * @param key 对话名词/对话ID
     * @return                 对话信息
     * @throws Exception       如果遇到任何问题,则抛出异常.
     */
    public ConversationInfo getConversationInfo (String key) throws Exception {
        ConversationInfo return_info = new ConversationInfo();
        return_info.Name = protocol.getChannelName(conversations.get(key));
        return_info.ID = conversations.get(key);
        return return_info;
    }
    /**
     * 取bot是否在线
     *
     * @return                 是否在线 true 在线  false 离线
     * @throws Exception       如果遇到任何问题,则抛出异常.
     */
    public boolean isCozeBotOnline() throws Exception {
        return protocol.isUserOnline(config.botID);
    }
    /**
     * 取出最后一次发送消息时间
     * @return 返回 Instant 类型  如果没有记录 返回类创建时间
     */
    public Instant getLatestSendMsgInstant () {
        return this.MessageListener.getLatestSendMsgInstant();
    }
    /**
     * 取出最后一次接收Coze Bot消息时间
     * @return 返回 Instant 类型  如果没有记录 返回类创建时间
     */
    public Instant getLatestReceiveCozeMsgInstant () {
        return this.MessageListener.getLatestReceiveCozeMsgInstant();
    }

}
