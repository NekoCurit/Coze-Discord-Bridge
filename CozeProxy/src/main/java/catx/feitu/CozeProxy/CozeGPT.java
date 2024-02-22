package catx.feitu.CozeProxy;

import catx.feitu.CozeProxy.ConversationManage.ConversationData;
import catx.feitu.CozeProxy.Exceptions.*;
import catx.feitu.CozeProxy.FunctionalInterface.ChatStreamEvent;
import catx.feitu.CozeProxy.Listen.CozeEventListener;
import catx.feitu.CozeProxy.LockManage.LockManage;
import catx.feitu.CozeProxy.MessageManage.BotResponseType;
import catx.feitu.CozeProxy.Protocol.ProtocolUtil;
import catx.feitu.CozeProxy.Protocol.Types.UploadFile;
import catx.feitu.CozeProxy.Protocol.UniversalEventListenerConfig;
import catx.feitu.CozeProxy.Types.ConversationInfo;
import catx.feitu.CozeProxy.Types.GPTFile;
import catx.feitu.CozeProxy.Types.GenerateMessage;

import java.io.ByteArrayInputStream;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CozeGPT {
    private final CozeGPTConfig config;
    private final LockManage LockManage = new LockManage();
    public CozeEventListener cozeEventListener;
    public ConversationData conversations = new ConversationData();
    public ProtocolUtil protocol = new ProtocolUtil();


    public CozeGPT(CozeGPTConfig config) {
        this.config = config;
    }
    public CozeGPT(CozeGPTConfig config,boolean autoLogin) throws Exception {
        this.config = config;
        if (autoLogin) {
            this.login();
        }
    }
    /**
     * Discord登录
     */
    public void login() throws Exception {
        cozeEventListener = new CozeEventListener();
        protocol.setConfig(new UniversalEventListenerConfig(config.serverID ,config.botID,!config.Disable_CozeBot_ReplyMsgCheck));
        protocol.eventListener = cozeEventListener;
        protocol.login(config.loginApp ,config.token ,config.Proxy);
    }
    /**
     * Discord登出
     *
     * @throws Exception       如果Bot未登录,可能会抛出 BotNotLoginException 异常
     */
    public void disconnect() throws Exception {
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
     * @param conversationName   对话ID/名称,可通过 CreateConversation(String name); 获取,必须提供,用于储存上下文信息.
     * @param Files            上传本地文件,可为多个.
     * @param event            填入以支持消息流返回,返回 true 继续生成,返回 false 停止生成.
     * @return                 生成的消息信息.
     * @throws Exception       如果消息生成过程遇到任何问题,则抛出异常.
     */
    public catx.feitu.CozeProxy.Types.GenerateMessage chat(String Prompts, String conversationName, List<GPTFile> Files, ChatStreamEvent event) throws Exception {
        if (Objects.equals(Prompts, "") || Prompts == null) {
            throw new InvalidPromptException();
        }
        String conversationID = conversations.get(conversationName);
        // 锁定 避免同频道多次对话
        LockManage.getLock(conversationID).lock();
        // 初始化回复记录
        cozeEventListener.botGenerateStatusManage.clearGenerateStatus(conversationID);
        cozeEventListener.botResponseManage.clearMsg(conversationID);
        // 开始
        try {
            String sendMessage = protocol.code.mentionUser(config.botID);
            List<UploadFile> sendFiles = new ArrayList<>();
            if (Prompts.length() > 2000) { // 长文本发送消息
                if (!config.Disable_2000Limit_Unlock) {
                    throw new PromptTooLongException(Prompts,2000);
                }

                sendMessage += config.promptOnSendMore2000Prefix;
                sendFiles.add(new UploadFile(new ByteArrayInputStream(Prompts.getBytes()),config.promptOnSendMore2000FileName));
            } else {
                sendMessage += Prompts;
            }
            // 发送附件(图片)处理
            if (Files != null)  {
                for (GPTFile file : Files) {
                    sendFiles.add(new UploadFile(file.GetByteArrayInputStream() ,file.GetFileName()));
                }
            }
            protocol.sendMessage(conversationID ,sendMessage ,sendFiles);
            // 在此之下为bot回复消息处理阶段 -> 5s 超时
            boolean BotStartGenerate = false;
            int attempt = 0; // 重试次数
            int maxRetries = 25; // 最大尝试次数
            /*
            while (!BotStartGenerate) {
                attempt++;
                if (attempt > maxRetries) {
                    throw new RecvMsgException("超时无回应:未开始生成");
                }
                BotStartGenerate = cozeEventListener.botGenerateStatusManage.getGenerateStatus(conversationID);
                System.out.println(BotStartGenerate + conversationID);
                // 等待200ms
                try { Thread.sleep(200); } catch (InterruptedException ignored) {}
            }
            */
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
                    Response = cozeEventListener.botResponseManage.getMsg(conversationID);
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
            LockManage.getLock(conversationID).unlock();
            return return_info;
        } catch (Exception e) {
            LockManage.getLock(conversationID).unlock();
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
    public GenerateMessage chat(String Prompts, String ConversationID, List<GPTFile> Files) throws Exception {
        return chat(Prompts ,ConversationID ,Files ,(ALLGenerateMessages, NewGenerateMessage) -> { return true; });
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
    public GenerateMessage chat(String Prompts, String ConversationID, ChatStreamEvent event) throws Exception {
        return chat(Prompts ,ConversationID ,null ,event);
    }
    /**
     * 发送一条消息到对话列表并等待Bot回复
     *
     * @param Prompts          提示词,即用户发送的消息,可以填入url上传附件.
     * @param ConversationID   对话ID,可通过 CreateConversation(String name); 获取,必须提供,用于储存上下文信息.
     * @return                 生成的消息信息.
     * @throws Exception       如果消息生成过程遇到任何问题,则抛出异常.
     */
    public GenerateMessage chat(String Prompts, String ConversationID) throws Exception {
        return chat(Prompts ,ConversationID ,(ALLGenerateMessages, NewGenerateMessage) -> { return true; });
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
        String conversationID = protocol.createChannel(conversationName ,config.Discord_CreateChannel_Category);
        protocol.inviteBotInChannel(conversationID ,config.botID);
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
         return createConversation(null);
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
        return cozeEventListener.getLatestSendMessageInstant();
    }
    /**
     * 取出最后一次接收Coze Bot消息时间
     * @return 返回 Instant 类型  如果没有记录 返回类创建时间
     */
    public Instant getLatestReceiveCozeMsgInstant () {
        return cozeEventListener.getLatestReceiveMessageInstant();
    }

}
