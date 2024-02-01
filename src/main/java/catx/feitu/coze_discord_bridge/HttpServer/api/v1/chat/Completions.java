package catx.feitu.coze_discord_bridge.HttpServer.api.v1.chat;

import catx.feitu.coze_discord_bridge.Config.ConfigManage;
import catx.feitu.coze_discord_bridge.Discord.Discord;
import catx.feitu.coze_discord_bridge.HttpServer.APIHandler;
import catx.feitu.coze_discord_bridge.HttpServer.HandleType;
import catx.feitu.coze_discord_bridge.HttpServer.ResponseType;
import catx.feitu.coze_discord_bridge.Misc.BotReplyType;
import catx.feitu.coze_discord_bridge.Misc.CacheManager;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.javacord.api.entity.channel.Channel;
import org.javacord.api.entity.channel.ChannelCategory;
import org.javacord.api.entity.channel.ServerChannel;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.server.Server;

import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;

public class Completions implements APIHandler {
    // https://platform.openai.com/docs/api-reference/chat/create
    private static final Logger logger = LogManager.getLogger(Completions.class);

    @Override
    public ResponseType handle(HandleType Handle) {
        JSONObject json = new JSONObject(true);
       if (!Handle.RequestParams.containsKey("messages")) {
            ResponseType ResponseType = new ResponseType();
            ResponseType.code = 400;
            JSONObject error = new JSONObject();
            error.put("message","无效的请求");
            error.put("type","one_api_error");
            json.put("error", error);
            ResponseType.msg = json.toJSONString();
            return ResponseType;
        }
        boolean UsingStream = Handle.RequestParams.containsKey("stream") ? Handle.RequestParams.getBoolean("stream") : true;
        String Channel_id = "";
        List<String> SendMessage = new ArrayList<>(); // 待发送消息列表
        List<String> OldMessage = new ArrayList<>();
        JSONArray messagesArray = Handle.RequestParams.getJSONArray("messages");
        String[] ForceDefaultModel = {"gpt4", "gpt-4-0314", "gpt-4-0613",
                "gpt-4-32k", "gpt-4-32k-0314", "gpt-4-32-0613", "gpt-4-turbo-preview",
                "gpt-4-1106-preview", "gpt-4-0125-preview", "gpt-4-vision-preview",
                "gpt-3.5-turbo", "gpt-3.5-turbo-0301", "gpt-3.5-turbo-0613",
                "gpt-3.5-turbo-1196", "gpt-3.5-turbo-16k-0613", "gemini-pro"};

        switch (ConfigManage.Configs.OpenAPI_Chat_Default_Models2Conversation) {
            case 0: // 通过读取 model 参数 , 如果 model 是OpenAI已存在的模型 那就通过传递过来的上下文自动判断
                for (int i = 0; i < messagesArray.size(); i++) {
                    JSONObject messageObject = messagesArray.getJSONObject(i);
                    // 取出role和content字段
                    if (Objects.equals(messageObject.getString("role"), "user") || Objects.equals(messageObject.getString("role"), "assistant")) {
                        OldMessage.add("[" + ConfigManage.Configs.OpenAPI_Chat_Default_Models2Conversation + "]" + messageObject.getString("role") + ":" + messageObject.getString("content"));
                    }
                }
                if (Arrays.asList(ForceDefaultModel).contains(Handle.RequestParams.getString("model"))) {
                    if (OldMessage.size() > 1) {
                        List<String> OldMessagePost = OldMessage;
                        OldMessagePost.remove(OldMessagePost.size() - 1); // 去掉用户最新发送消息
                        Channel_id = CacheManager.Cache_Default_OldMessage2Name(OldMessagePost);
                    }
                    Channel_id = Objects.equals(Channel_id, "") ? RandomName() : Channel_id;
                } else {
                    Channel_id = Handle.RequestParams.getString("model");
                }
                if (OldMessage.size() == 1) { // 去除SystemPrompt后 聊天记录长度 = 1 第一次发消息/清空过历史记录了
                    CacheManager.Cache_Default_NameDeleteOldMessage (Channel_id);
                    for (int i = 0; i < messagesArray.size(); i++) { // 系统提示词也发送 如果是第一次的话
                        JSONObject messageObject = messagesArray.getJSONObject(i);
                        if (Objects.equals(messageObject.getString("role"), "system")) {
                            SendMessage.add(messageObject.getString("content"));
                        }
                    }
                }
                if (!OldMessage.isEmpty()) {
                    SendMessage.add(OldMessage.get(OldMessage.size() - 1)); // 最后一条消息
                }
                break;
            case 1:
                List<String> SendMessageL = new ArrayList<>();
                for (int i = 0; i < messagesArray.size(); i++) {
                    JSONObject messageObject = messagesArray.getJSONObject(i);
                    SendMessageL.add(messageObject.getString("role") + ":" + messageObject.getString("content"));
                }
                Channel_id = Arrays.asList(ForceDefaultModel).contains(Handle.RequestParams.getString("model")) ?
                        CacheManager.Cache_GetName2Channel(ConfigManage.Configs.OpenAPI_Chat_Default_Channel) :
                        Handle.RequestParams.getString("model");
                SendMessage.add(ConfigManage.Configs.OpenAPI_Chat_MsgForward_Prefix
                        + "\n\n" + String.join("\n",SendMessageL)
                        + "\n\n" + ConfigManage.Configs.OpenAPI_Chat_MsgForward_Suffix); // 最后一条消息
                break;
            case 2:
                Channel_id = Handle.RequestParams.getString("model");
                String SendMessageOne = "";
                for (int i = 0; i < messagesArray.size(); i++) {
                    JSONObject messageObject = messagesArray.getJSONObject(i);
                    SendMessageOne = messageObject.getString("content");
                }
                SendMessage.add(SendMessageOne);
                break;
            case 3:
                List<String> SendMessageR = new ArrayList<>();
                for (int i = 0; i < messagesArray.size(); i++) {
                    JSONObject messageObject = messagesArray.getJSONObject(i);
                    SendMessageR.add(messageObject.getString("role") + ":" + messageObject.getString("content"));
                }
                Channel_id = CacheManager.Cache_GetName2Channel(ConfigManage.Configs.OpenAPI_Chat_Default_Channel);
                SendMessage.add(ConfigManage.Configs.OpenAPI_Chat_MsgForward_Prefix
                        + "\n\n" + String.join("\n",SendMessageR)
                        + "\n\n" + ConfigManage.Configs.OpenAPI_Chat_MsgForward_Suffix); // 最后一条消息
                break;
        }
        if (SendMessage.isEmpty()) { // 为空 错误
            ResponseType ResponseType = new ResponseType();
            ResponseType.code = 400;
            JSONObject error = new JSONObject();
            error.put("message","未知错误");
            error.put("type","one_api_error");
            json.put("error", error);
            ResponseType.msg = json.toJSONString();
            return ResponseType;
        }

        Optional<Server> optionalServer = Discord.api.getServerById(ConfigManage.Configs.CozeBot_InServer_id);
        if (optionalServer.isEmpty()) {
            ResponseType ResponseType = new ResponseType();
            ResponseType.code = 502;
            JSONObject error = new JSONObject();
            error.put("message","服务端配置异常:CozeBot_InServer_id没有找到匹配的Discord服务器");
            error.put("type","one_api_error");
            json.put("error", error);
            logger.warn("执行失败:服务端配置异常:CozeBot_InServer_id没有找到匹配的Discord服务器");
            ResponseType.msg = json.toJSONString();
            return ResponseType;
        } else {
            Handle.HttpExchange_Disable_Default_Action = true;
            // 获取聊天对应频道 不存在则创建
            Optional<ServerChannel> ChannelTest = optionalServer.get().getChannelById(CacheManager.Cache_GetName2Channel(Channel_id));
            TextChannel Channel;
            if (ChannelTest.isEmpty()) {// 子频道id不存在 创建
                Optional<org.javacord.api.entity.channel.Channel> Category = Discord.api.getChannelById(ConfigManage.Configs.Discord_CreateChannel_Category);
                ChannelCategory category = (ChannelCategory) Category.orElse(null);

                Channel = optionalServer.get().createTextChannelBuilder()
                        .setName(Channel_id)
                        .setCategory(category)
                        .create()
                        .join();
                CacheManager.Cache_AddName2Channel(ConfigManage.Configs.OpenAPI_Chat_Default_Channel,Channel.getIdAsString());
            } else {
                Optional<TextChannel> ChannelTest2 = ChannelTest.get().asTextChannel();
                Channel = ChannelTest2.isEmpty() ? // 是否为文字频道
                        // 为空 = 不是  创建
                        optionalServer.get().createTextChannelBuilder()
                                .setName(Channel_id)
                                .create()
                                .join() :
                        // 不为空 = 是  读取
                        ChannelTest2.get();
            }
            try {
                BotReplyType Reply = new BotReplyType();
                String LatestTextMessage = "";
                OutputStream os = Handle.HttpExchange.getResponseBody();
                // 提前配置json
                json.put("id", "chatcmpl-" + CacheManager.Cache_GetName2Channel(Channel_id));
                json.put("object", "chat.completion.chunk"); // 流式返回标识
                json.put("created", Instant.now().toEpochMilli() / 1000);
                json.put("model", Handle.RequestParams.getString("model"));
                json.put("system_fingerprint", null);
                JSONObject choice = new JSONObject(true);
                choice.put("index", 0);
                JSONObject delta = new JSONObject(true);
                delta.put("role", "assistant");
                delta.put("content", "");
                choice.put("delta", delta);
                choice.put("logprobs", null);
                choice.put("finish_reason", null);
                json.put("choices", new JSONObject[]{choice});
                if(UsingStream) {
                    Handle.HttpExchange.getResponseHeaders().set("Content-Type", "text/event-stream");

                    Handle.HttpExchange.getResponseHeaders().set("Openai-Model", Handle.RequestParams.getString("model"));
                    Handle.HttpExchange.getResponseHeaders().set("Openai-Organization", "user-default");
                    Handle.HttpExchange.getResponseHeaders().set("Openai-Processing-Ms", "100");
                    Handle.HttpExchange.getResponseHeaders().set("Openai-Version", "2024-01-01");


                    Handle.HttpExchange.getResponseHeaders().set("Cache-Control", "no-cache, must-revalidate");


                    Handle.HttpExchange.getResponseHeaders().add("Transfer-Encoding", "chunked");
                    Handle.HttpExchange.sendResponseHeaders(200, 0);

                    os.write(("data: " + json.toJSONString() + "\n\n").getBytes(StandardCharsets.UTF_8));
                    os.flush();
                }
                logger.info(SendMessage.size());
                int retryInterval = 200; // 等待时间间隔 单位为毫秒
                int maxRetries = 300; // 最大尝试次数
                int attempt = 0; // 当前尝试次数
                for (int i = 0;i < SendMessage.size() - 2;i++) { // 减去 定义域从0开始 和 最后一条待发送
                    // 发送消息
                    Channel.sendMessage("<@" + ConfigManage.Configs.CozeBot_id + ">" + SendMessage.get(i)).join();
                    while (!Reply.Done) {
                        attempt++;
                        Reply = CacheManager.Cache_BotReplyMessageEx(Channel.getIdAsString());
                        if (attempt < maxRetries) {
                            try { Thread.sleep(retryInterval); } catch (InterruptedException ignored) {}
                        } else { break; }
                    }
                    attempt = 0;
                }
                logger.info(3);
                Channel.sendMessage("<@" + ConfigManage.Configs.CozeBot_id + ">" + SendMessage.get(SendMessage.size() - 1)).join();
                while (!Reply.Done) {
                    if (UsingStream) { // 启用流式返回
                        attempt++;
                        Reply = CacheManager.Cache_BotReplyMessageEx(Channel.getIdAsString());
                        if (attempt < maxRetries) {
                            try { Thread.sleep(retryInterval); } catch (InterruptedException ignored) {}
                        } else { break; }
                        //if (!LatestTextMessage.equals(Reply.TextMessage)) { // 生成了更多内容 流式返回
                            // 流式返回 JSON
                            JSONObject update_choice = new JSONObject(true);
                            update_choice.put("index", 0);
                            JSONObject update_delta = new JSONObject(true);
                            update_delta.put("content", Reply.TextMessage.replace(LatestTextMessage, "")); // 只需要发送新生成的content就行了
                            LatestTextMessage = Reply.TextMessage;
                            update_choice.put("delta", update_delta);
                            json.put("choices", new JSONObject[]{update_choice});
                            logger.info(json.toJSONString());
                            // 负责发送 发送失败直接抛出异常关闭连接
                            os.write(("data: " + json.toJSONString() + "\n\n").getBytes(StandardCharsets.UTF_8));

                            os.flush();
                        //}
                    }
                }
                json.put("object", "chat.completion"); // 聊天完成标识
                delta.put("content",Reply.TextMessage);
                choice.put("index", 0);
                choice.put("delta", delta);
                choice.put("finish_reason", "stop");
                json.put("choices", new JSONObject[]{choice});
                JSONObject usage = new JSONObject(true);
                usage.put("prompt_tokens", 0);
                usage.put("completion_tokens", 0);
                usage.put("total_tokens", 0);
                choice.put("usage", usage);
                if (UsingStream) {
                    os.write(("data: [DONE]\n\n").getBytes(StandardCharsets.UTF_8));
                }
                else { // 不使用流式传输
                    byte[] bytes = json.toJSONString().getBytes();
                    Handle.HttpExchange.sendResponseHeaders(200, bytes.length);
                    os.write(bytes);
                }
                os.flush();
                os.close();
                Handle.RequestParams.getString("model");
                if (ConfigManage.Configs.OpenAPI_Chat_Default_Models2Conversation == 0) {
                    CacheManager.Cache_Default_NameWriteOldMessage(Channel_id, OldMessage);
                }
            } catch (Exception e) {
                logger.error ("error",e);
            }


        }
        return null;
    }
    public static String RandomName() {
        Random random = new Random();
        StringBuilder sb = new StringBuilder(16);
        String characters = "0123456789abcdef";
        for(int i=0; i<16; i++){
            int index = random.nextInt(characters.length());
            sb.append(characters.charAt(index));
        }
        return(sb.toString());
    }
}
