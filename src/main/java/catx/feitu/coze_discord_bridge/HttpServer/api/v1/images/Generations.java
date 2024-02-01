package catx.feitu.coze_discord_bridge.HttpServer.api.v1.images;

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
import org.javacord.api.entity.channel.ChannelCategory;
import org.javacord.api.entity.channel.ServerChannel;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.server.Server;

import java.time.Instant;
import java.util.*;

public class Generations implements APIHandler {
    private static final Logger logger = LogManager.getLogger(catx.feitu.coze_discord_bridge.HttpServer.api.v1.chat.Completions.class);
    @Override
    public ResponseType handle(HandleType Handle) {
        ResponseType ResponseType = new ResponseType();
        JSONObject json = new JSONObject(true);
        if (!Handle.RequestParams.containsKey("prompt")) {
            ResponseType.code = 400;
            JSONObject error = new JSONObject();
            error.put("message","prompt为空或不存在");
            error.put("type","one_api_error");
            json.put("error", error);
            ResponseType.msg = json.toJSONString();
            return ResponseType;
        }
        String Channel_id = CacheManager.Cache_GetName2Channel(ConfigManage.Configs.OpenAPI_ImageGenerate_Default_Channel);

        Optional<Server> optionalServer = Discord.api.getServerById(ConfigManage.Configs.CozeBot_InServer_id);
        if (optionalServer.isEmpty()) {
            ResponseType.code = 502;
            JSONObject error = new JSONObject();
            error.put("message","服务端配置异常:CozeBot_InServer_id没有找到匹配的Discord服务器");
            error.put("type","one_api_error");
            json.put("error", error);
            logger.warn("执行失败:服务端配置异常:CozeBot_InServer_id没有找到匹配的Discord服务器");
            ResponseType.msg = json.toJSONString();
            return ResponseType;
        } else {
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
                int retryInterval = 200; // 等待时间间隔 单位为毫秒
                int maxRetries = 300; // 最大尝试次数
                int attempt = 0; // 当前尝试次数

                Channel.sendMessage("<@" + ConfigManage.Configs.CozeBot_id + ">" +Handle.RequestParams.getString("prompt")).join();
                while (!Reply.Done) {
                    attempt++;
                    Reply = CacheManager.Cache_BotReplyMessageEx(Channel.getIdAsString());
                    if (attempt > maxRetries) {
                        break;
                    }
                    try { Thread.sleep(retryInterval); } catch (InterruptedException ignored) {}
                }
                JSONArray dataArray = new JSONArray();
                json.put("created", Instant.now().toEpochMilli() / 1000);
                for(int i = 0;Reply.Files.size() > i;i++) {
                    JSONObject dataItem1 = new JSONObject();
                    dataItem1.put("url", Reply.Files.get(i));
                    dataArray.add(dataItem1);
                }
                json.put("data", dataArray);
                ResponseType.code = 200;
                ResponseType.msg = json.toJSONString();
                return ResponseType;
            } catch (Exception ignored) { }
        }
        return null;
    }
}
