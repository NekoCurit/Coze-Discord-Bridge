package catx.feitu.CozeProxy;

import catx.feitu.CozeProxy.exception.*;
import catx.feitu.CozeProxy.Interface.ChatStreamEvent;
import catx.feitu.CozeProxy.listen.EventHandle;
import catx.feitu.CozeProxy.data.Data;
import catx.feitu.CozeProxy.impl.response.Response;
import catx.feitu.CozeProxy.protocol.ProtocolHandle;
import catx.feitu.CozeProxy.protocol.impl.UploadFile;
import catx.feitu.CozeProxy.protocol.listene.EventListenConfig;
import catx.feitu.CozeProxy.impl.ConversationInfo;
import catx.feitu.CozeProxy.impl.GPTFile;
import catx.feitu.CozeProxy.impl.GenerateMessage;
import catx.feitu.CozeProxy.utils.BotResponseStatusCode;
import catx.feitu.CozeProxy.utils.ProtocolUtils;
import catx.feitu.CozeProxy.utils.Utils;
import catx.feitu.CozeProxy.utils.extensions.Protocol;

import java.io.ByteArrayInputStream;
import java.net.Proxy;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

public class CozeGPT {
    public Data data = new Data();
    public Utils utils = new Utils(data);
    private final CozeGPTConfig config;
    public EventHandle eventListen;

    public List<Protocol> protocol = new CopyOnWriteArrayList<>();


    public CozeGPT(CozeGPTConfig config) {
        eventListen = new EventHandle(utils);
        this.config = config;
    }
    /**
     * 登录一个新的Discord账号
     * @param token DiscordUser账号Token
     * @param proxy 自定义代理配置 null使用配置中默认代理
     * @throws Exception 如果登录过程遇到任何问题,则抛出异常.
     */
    public void login(String token, Proxy proxy) throws Exception {
        Protocol protocolJoin = new Protocol();

        protocolJoin.setConfig(new EventListenConfig(config.serverID ,config.botID,!config.Disable_CozeBot_ReplyMsgCheck));
        protocolJoin.login(config.loginApp ,token ,proxy == null ? config.Proxy : proxy);

        protocol.add(protocolJoin);
    }
    /**
     * 登录一个新的Discord账号
     * @param token DiscordUser账号Token
     * @throws Exception 如果登录过程遇到任何问题,则抛出异常.
     */
    public void login(String token) throws Exception {
        login(token,null);
    }
    /**
     * 批量登录新的Discord账号
     * @param tokens DiscordUser账号Token列表
     * @param ignoredException 是否忽略异常 为否一个账号登录失败则直接throw
     * @param proxy 自定义代理配置 null使用配置中默认代理
     * @return 登录成功的账号数
     * @throws Exception 如果登录过程遇到任何问题,则抛出异常.
     */
    public int login(List<String> tokens, boolean ignoredException, Proxy proxy) throws Exception {
        int i = 0;
         for (String token : tokens) {
             try {
                 Protocol protocolJoin = new Protocol();

                 protocolJoin.setConfig(new EventListenConfig(config.serverID ,config.botID,!config.Disable_CozeBot_ReplyMsgCheck));
                 protocolJoin.login(config.loginApp ,token ,proxy == null ? config.Proxy : proxy);

                 protocol.add(protocolJoin);
                 i++;
             } catch (Exception e) {
                 if (!ignoredException) {
                     throw e;
                 }
             }
        }
        return i;
    }
    /**
     * 批量登录新的Discord账号
     * @param tokens DiscordUser账号Token列表
     * @param ignoredException 是否忽略异常 为否一个账号登录失败则直接throw
     * @return 登录成功的账号数
     * @throws Exception 如果登录过程遇到任何问题,则抛出异常.
     */
    public int login(List<String> tokens, boolean ignoredException) throws Exception {
        return login(tokens, ignoredException, null);
    }
    /**
     * 批量登录新的Discord账号
     * @param tokens DiscordUser账号Token列表
     * @param proxy 自定义代理配置 null使用配置中默认代理
     * @return 登录成功的账号数
     */
    public int login(List<String> tokens, Proxy proxy) {
        try {
            return login(tokens, true,proxy);
        } catch (Exception ignored) {
            return 0;
        }
    }
    /**
     * 批量登录新的Discord账号
     * @param tokens DiscordUser账号Token列表
     * @return 登录成功的账号数
     */
    public int login(List<String> tokens) {
        try {
            return login(tokens, true);
        } catch (Exception ignored) {
            return 0;
        }
    }
    /**
     * 获取已登录账号数
     * @return 已登录账号数
     */
    public int getLoginCount() {
        return protocol.size();
    }
    /**
     * 退出登录并删除全部账号
     */
    public void disconnectAll() {
        for (ProtocolHandle protocol : protocol) {
            try { protocol.disconnect(); } catch (Exception ignored) { }
        }
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
    public catx.feitu.CozeProxy.impl.GenerateMessage chat(String Prompts, String conversationName, List<GPTFile> Files, ChatStreamEvent event) throws Exception {
        if (Objects.equals(Prompts, "") || Prompts == null) {
            throw new InvalidPromptException();
        }
        String conversationID = utils.conversation.getId(conversationName);
        // 锁定 避免同频道多次对话
        utils.lock.getLock(conversationID).lock();
        // 初始化回复记录
        utils.generateStatus.clearGenerateStatus(conversationID);
        utils.response.clearMsg(conversationID);
        // 开始
        try {
            boolean isDone = false;
            GenerateMessage responseMessage = new GenerateMessage();

            List<Protocol> protocols = ProtocolUtils.getAliveProtocols(protocol);

            while (!isDone) {
                Protocol selectedProtocol = protocols.get(0);

                String sendMessage = selectedProtocol.code.mentionUser(config.botID);
                List<UploadFile> sendFiles = new ArrayList<>();
                if (Prompts.length() > 2000) { // 长文本发送消息
                    if (!config.Disable_2000Limit_Unlock) {
                        throw new PromptTooLongException(Prompts, 2000);
                    }

                    sendMessage += config.promptOnSendMore2000Prefix;
                    sendFiles.add(new UploadFile(new ByteArrayInputStream(Prompts.getBytes()), config.promptOnSendMore2000FileName));
                } else {
                    sendMessage += Prompts;
                }
                // 发送附件(图片)处理
                if (Files != null) {
                    for (GPTFile file : Files) {
                        sendFiles.add(new UploadFile(file.GetByteArrayInputStream(), file.GetFileName()));
                    }
                }

                selectedProtocol.eventListen = eventListen;

                selectedProtocol.sendMessage(conversationID, sendMessage, sendFiles);
                // 在此之下为bot回复消息处理阶段 -> 5s 超时
                //boolean BotStartGenerate = false;
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
                Response Response = new Response();
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
                        Response = utils.response.getMsg(conversationID);
                    } catch (NullPointerException ignored) {
                    }
                    if (!event.handle(Response.prompt, Response.prompt.replace(LatestMessage, ""))) {
                        throw new StopGenerateException();
                    }
                    LatestMessage = Response.prompt;
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException ignored) {
                    }
                }

                selectedProtocol.eventListen = null;

                responseMessage.Message = Response.prompt;
                responseMessage.Files = Response.files;

                if (Objects.equals(responseMessage.Message, BotResponseStatusCode.TRY_A_BIT_LATER)) continue;
                if (Objects.equals(responseMessage.Message, BotResponseStatusCode.TRY_TOMORROW)) {
                    selectedProtocol.limitedDate = new Date();
                    protocols.remove(0);
                }

                isDone = true;
                selectedProtocol.chatCountTotal++;
            }
            utils.lock.getLock(conversationID).unlock();
            return responseMessage;
        } catch (Exception e) {
            utils.lock.getLock(conversationID).unlock();
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
        // ID与名称相同会造成问题 需要检查
        String id = utils.conversation.getId(conversationName);

        List<Exception> exceptions = new ArrayList<>();
        for (ProtocolHandle protocol : protocol) {
            try {
                if (protocol.isChannelFound(id)) {
                    throw new ConversationAlreadyExistsException(conversationName);
                }
                String conversationID = protocol.createChannel(conversationName ,config.Discord_CreateChannel_Category);
                // 写入存储
                utils.conversation.put(conversationName ,conversationID);
                // 返回数据
                return conversationID;
            } catch (Exception e) {
                exceptions.add(e);
            }
        }
        throw new RunningFailedException(exceptions,conversationName);
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
     * @param key 对话名称/对话ID
     * @throws Exception 如果遇到任何问题,则抛出异常.
     */
    public void deleteConversation (String key) throws Exception {
        List<Exception> exceptions = new ArrayList<>();
        for (ProtocolHandle protocol : protocol) {
            try {
                protocol.deleteChannel(utils.conversation.getId(key));
                deleteConversationLocal(key);
            } catch (Exception e) {
                exceptions.add(e);
            }
        }
        throw new RunningFailedException(exceptions,key);
    }
    /**
     * 在本地缓存中删除对话列表索引 速度更快
     *
     * @param conversationName 对话名词/对话ID
     */
    public void deleteConversationLocal (String conversationName) {
        utils.conversation.remove(conversationName);
    }
    /**
     * 修改某个对话的名称
     * 注:对话ID无法修改
     *
     * @param oldConversationName 旧的对话名词/对话ID
     * @param newConversationName 新的对话名词
     * @throws Exception          如果遇到任何问题,则抛出异常.
     */
    public String renameConversation (String oldConversationName, String newConversationName) throws Exception {
        String conversationID = utils.conversation.getId(oldConversationName);
        List<Exception> exceptions = new ArrayList<>();
        for (ProtocolHandle protocol : protocol) {
            try {
                protocol.setChannelName(conversationID ,newConversationName);
                return renameConversationLocal(oldConversationName, newConversationName);
            } catch (Exception e) {
                exceptions.add(e);
            }
        }
        throw new RunningFailedException(exceptions,oldConversationName);
    }
    /**
     * 仅在本地索引中修改某个对话的名称
     * 注:对话ID无法修改
     *
     * @param oldConversationName 旧的对话名词/对话ID
     * @param newConversationName 新的对话名词
     * @throws Exception          如果遇到任何问题,则抛出异常.
     */
    public String renameConversationLocal (String oldConversationName, String newConversationName) throws Exception {
        String conversationID = utils.conversation.getId(oldConversationName);

        utils.conversation.put(newConversationName ,conversationID);
        utils.conversation.remove(oldConversationName);

        return conversationID;
    }
    /**
     * 获取某个对话的信息
     *
     * @param key 对话名称/对话ID
     * @param noCache 不通过缓存获取
     * @return                 对话信息
     * @throws Exception       如果遇到任何问题,则抛出异常.
     */
    public ConversationInfo getConversationInfo (String key,Boolean noCache) throws Exception {
        ConversationInfo response = new ConversationInfo();

        response.ID = utils.conversation.getId(key);

        if (noCache) {
            for (ProtocolHandle protocol : protocol) {
                // 获取失败(异常) 就尝试下一个token
                try {
                    response.Name = protocol.getChannelName(response.ID);
                } catch (Exception ignored) { }
            }
        } else {
            response.Name = utils.conversation.getName(response.ID);
        }

        if (response.Name == null) throw new InvalidConversationException(response.ID);

        return response;
    }
    /**
     * 获取某个对话的信息
     *
     * @param key 对话名称/对话ID
     * @return                 对话信息
     * @throws Exception       如果遇到任何问题,则抛出异常.
     */
    public ConversationInfo getConversationInfo (String key) throws Exception {
        return getConversationInfo(key,false);
    }
    /**
     * 从本地索引获取某个对话的信息
     *
     * @param key 对话名称/对话ID
     * @return                 对话信息
     * @throws Exception       如果遇到任何问题,则抛出异常.
     */
    public ConversationInfo getConversationInfoLocal (String key) throws Exception {
        return getConversationInfo(key,true);
    }
    /**
     * 取bot是否在线
     *
     * @return                 是否在线 true 在线  false 离线
     * @throws Exception       如果遇到任何问题,则抛出异常.
     */
    public boolean isCozeBotOnline() throws Exception {
        List<Exception> exceptions = new ArrayList<>();
        for (ProtocolHandle protocol : protocol) {
            try {
                return protocol.isUserOnline(config.botID);
            } catch (Exception e) {
                exceptions.add(e);
            }
        }
        throw new RunningFailedException(exceptions,null);
    }
    /**
     * 取出最后一次发送消息时间
     * @return 返回 Instant 类型  如果没有记录 返回类创建时间
     */
    public Instant getLatestSendMsgInstant () {
        return eventListen.getLatestSendMessageInstant();
    }
    /**
     * 取出最后一次接收Coze Bot消息时间
     * @return 返回 Instant 类型  如果没有记录 返回类创建时间
     */
    public Instant getLatestReceiveCozeMsgInstant () {
        return eventListen.getLatestReceiveMessageInstant();
    }

}
