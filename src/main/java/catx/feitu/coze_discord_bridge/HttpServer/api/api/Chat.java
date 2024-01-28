package catx.feitu.coze_discord_bridge.HttpServer.api.api;

import catx.feitu.coze_discord_bridge.Misc.CacheManager;
import catx.feitu.coze_discord_bridge.Config.ConfigManage;
import catx.feitu.coze_discord_bridge.Discord.Discord;
import catx.feitu.coze_discord_bridge.HttpServer.APIHandler;
import catx.feitu.coze_discord_bridge.HttpServer.HandleType;
import catx.feitu.coze_discord_bridge.HttpServer.ResponseType;
import catx.feitu.coze_discord_bridge.Misc.LockManager;
import com.alibaba.fastjson.JSONObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.javacord.api.entity.channel.ServerChannel;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.server.Server;

import java.util.Objects;
import java.util.Optional;

public class Chat implements APIHandler {

    private static final Logger logger = LogManager.getLogger(Chat.class);
    @Override
    public ResponseType handle(HandleType Handle) {
        ResponseType Response = new ResponseType();
        JSONObject json = new JSONObject();
        Optional<Server> optionalServer = Discord.api.getServerById(ConfigManage.Configs.CozeBot_InServer_id);
        if (optionalServer.isEmpty()) {
            Response.code = 502;
            json.put("code", 502);
            json.put("message", "服务端配置异常:CozeBot_InServer_id没有找到匹配的Discord服务器");
            logger.warn("执行CreateConversation失败:服务端配置异常:CozeBot_InServer_id没有找到匹配的Discord服务器");
        }
        else {
            if (!Handle.RequestParams.containsKey("name")) {
                Response.code = 400;
                json.put("code", 400);
                json.put("message", "参数缺失:name");
                JSONObject json_data = new JSONObject();
                json.put("data", json_data);
            }
            if (!Handle.RequestParams.containsKey("prompt")) {
                Response.code = 400;
                json.put("code", 400);
                json.put("message", "参数缺失:prompt");
                JSONObject json_data = new JSONObject();
                json.put("data", json_data);
            }
            else {
                Optional<ServerChannel> channel = optionalServer.get().getChannelById(CacheManager.Cache_GetName2Channel(Handle.RequestParams.getString("name")));
                if (channel.isEmpty()) {
                    Response.code = 502;
                    json.put("code", 502);
                    json.put("message", "当前对话不存在");
                    JSONObject json_data = new JSONObject();
                    json_data.put("status", false);
                    json.put("data", json_data);
                } else {
                    if(channel.get() instanceof TextChannel) {
                        TextChannel textChannel = (TextChannel) channel.get();
                        // IDEA会提示 「 变量 'textChannel' 可被替换为模式变量 」  不要点 点了就build failed
                        // 误点补救处 复制替换上去即可
                        // if(channel.get() instanceof TextChannel) {
                        // TextChannel textChannel = (TextChannel) channel.get();
                        LockManager.getLock(channel.get().getIdAsString()).lock(); // 上锁
                        CacheManager.Cache_BotReplyClear(textChannel.getIdAsString());
                        textChannel.sendMessage("<@" + ConfigManage.Configs.CozeBot_id + ">" + Handle.RequestParams.getString("prompt")).join();
                        String Prompt = "";

                        int retryInterval = 200; // 等待时间间隔 单位为毫秒
                        int maxRetries = 100; // 最大尝试次数
                        int attempt = 0; // 当前尝试次数
                        while (Objects.equals(Prompt, "")) {
                            attempt++; // +1
                            Prompt = CacheManager.Cache_BotReplyGetPrompt(textChannel.getIdAsString());
                            if (attempt < maxRetries) {
                                try { Thread.sleep(retryInterval); } catch (InterruptedException ignored) {}
                            } else {
                                break;
                            }
                        }
                        LockManager.getLock(channel.get().getIdAsString()).unlock(); // 解锁
                        Response.code = 200;
                        json.put("code", 200);
                        json.put("message", "成功!");
                        JSONObject json_data = new JSONObject();
                        json_data.put("prompt", Prompt);
                        json.put("data", json_data);
                    } else {
                        Response.code = 502;
                        json.put("code", 502);
                        json.put("message", "执行失败:目标非文本频道");
                        JSONObject json_data = new JSONObject();
                        json.put("data", json_data);
                    }
                }
            }
        }
        Response.msg = json.toJSONString();
        Response.Header_Content_Type = "application/json; charset=UTF-8";

        return Response;
    }
}
