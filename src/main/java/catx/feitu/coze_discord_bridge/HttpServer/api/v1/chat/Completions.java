package catx.feitu.coze_discord_bridge.HttpServer.api.v1.chat;

import catx.feitu.CozeProxy.exception.InvalidConversationException;
import catx.feitu.CozeProxy.impl.GenerateMessage;
import catx.feitu.coze_discord_bridge.Config.ConfigManage;
import catx.feitu.coze_discord_bridge.HttpServer.APIHandler;
import catx.feitu.coze_discord_bridge.HttpServer.HandleType;
import catx.feitu.coze_discord_bridge.HttpServer.ResponseType;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class Completions implements APIHandler {
    // https://platform.openai.com/docs/api-reference/chat/create
    private static final Logger logger = LogManager.getLogger(Completions.class);

    @Override
    public ResponseType handle(HandleType Handle) {
        ResponseType Response = new ResponseType();

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
        JSONArray messagesArray = Handle.RequestParams.getJSONArray("messages");
        String[] ForceDefaultModel = {"gpt4", "gpt-4-0314", "gpt-4-0613",
                "gpt-4-32k", "gpt-4-32k-0314", "gpt-4-32-0613", "gpt-4-turbo-preview",
                "gpt-4-1106-preview", "gpt-4-0125-preview", "gpt-4-vision-preview",
                "gpt-3.5-turbo", "gpt-3.5-turbo-0301", "gpt-3.5-turbo-0613",
                "gpt-3.5-turbo-1196", "gpt-3.5-turbo-16k-0613", "gemini-pro"};

        switch (ConfigManage.configs.OpenAPI_Chat_Default_Models2Conversation) {
            case 0: // 通过读取 model 参数 , 如果 model 是OpenAI已存在的模型 那就通过传递过来的上下文自动判断
                String LatestMessage = "";
                for (int i = 0; i < messagesArray.size(); i++) {
                    JSONObject messageObject = messagesArray.getJSONObject(i);
                    // 取出role和content字段
                    if (Objects.equals(messageObject.getString("role"), "user")) {
                        LatestMessage = messageObject.getString("content");
                    }
                }
                Channel_id = ConfigManage.configs.OpenAPI_Chat_Default_Channel;
                SendMessage.add(LatestMessage); // 最后一条消息
                break;
            case 1:
                List<String> SendMessageL = new ArrayList<>();
                for (int i = 0; i < messagesArray.size(); i++) {
                    JSONObject messageObject = messagesArray.getJSONObject(i);
                    SendMessageL.add(messageObject.getString("role") + ":" + messageObject.getString("content"));
                }
                Channel_id = Arrays.asList(ForceDefaultModel).contains(Handle.RequestParams.getString("model")) ?
                        ConfigManage.configs.OpenAPI_Chat_Default_Channel:
                        Handle.RequestParams.getString("model");
                SendMessage.add(ConfigManage.configs.OpenAPI_Chat_MsgForward_Prefix
                        + "\n\n" + String.join("\n",SendMessageL)
                        + "\n\n" + ConfigManage.configs.OpenAPI_Chat_MsgForward_Suffix); // 最后一条消息
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
                Channel_id = ConfigManage.configs.OpenAPI_Chat_Default_Channel;
                SendMessage.add(ConfigManage.configs.OpenAPI_Chat_MsgForward_Prefix
                        + "\n\n" + String.join("\n",SendMessageR)
                        + "\n\n" + ConfigManage.configs.OpenAPI_Chat_MsgForward_Suffix); // 最后一条消息
                break;
        }
        logger.info(String.join("|",SendMessage));
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

        try {
            OutputStream os = Handle.HttpExchange.getResponseBody();
            // 提前配置json
            json.put("id", "chatcmpl-" + Channel_id);
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
            // 创建频道
            try { Handle.CozeGPT.getConversationInfo(Channel_id); }
            catch (InvalidConversationException e) { Handle.CozeGPT.createConversation(Channel_id);
            };
            GenerateMessage Generate = Handle.CozeGPT.chat(String.join("\n\n", SendMessage), Channel_id,(ALLGenerateMessages, NewGenerateMessage) -> {
                if (UsingStream) { // 启用流式返回
                    // 流式返回 JSON
                    JSONObject update_choice = new JSONObject(true);
                    update_choice.put("index", 0);
                    JSONObject update_delta = new JSONObject(true);
                    update_delta.put("content", NewGenerateMessage); // 只需要发送新生成的content就行了
                    update_choice.put("delta", update_delta);
                    json.put("choices", new JSONObject[]{update_choice});
                    try {
                        os.write(("data: " + json.toJSONString() + "\n\n").getBytes(StandardCharsets.UTF_8));
                        os.flush();
                    } catch (Exception e) { return false; } // 返回流失败 = 客户端断开连接 停止生成监听
                }
                return true;
            });
            if (UsingStream) {
                Handle.HttpExchange_Disable_Default_Action = true;
                os.write(("data: [DONE]\n\n").getBytes(StandardCharsets.UTF_8));
                os.close();
            } else {
                json.put("object", "chat.completion"); // 聊天完成标识
                delta.put("content",Generate.Message);
                choice.put("index", 0);
                choice.put("delta", delta);
                choice.put("finish_reason", "stop");
                json.put("choices", new JSONObject[]{choice});
                JSONObject usage = new JSONObject(true);
                usage.put("prompt_tokens", 0);
                usage.put("completion_tokens", 0);
                usage.put("total_tokens", 0);
                choice.put("usage", usage);
                Response.msg = json.toJSONString();
            }
            return Response;
        } catch (Exception e) {
            logger.error(e);
            Response.code = 400;
            JSONObject error = new JSONObject();
            error.put("message", e.getClass().getSimpleName());
            error.put("type", "one_api_error");
            json.put("error", error);
            Response.msg = json.toJSONString();
            return Response;
        }
    }
}
