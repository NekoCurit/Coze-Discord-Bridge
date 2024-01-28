package catx.feitu.coze_discord_bridge.HttpServer.api.api;

import catx.feitu.coze_discord_bridge.Misc.CacheManager;
import catx.feitu.coze_discord_bridge.Config.ConfigManage;
import catx.feitu.coze_discord_bridge.Discord.Discord;
import catx.feitu.coze_discord_bridge.HttpServer.APIHandler;
import catx.feitu.coze_discord_bridge.HttpServer.HandleType;
import catx.feitu.coze_discord_bridge.HttpServer.ResponseType;
import com.alibaba.fastjson.JSONObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.javacord.api.entity.channel.ServerChannel;
import org.javacord.api.entity.server.Server;

import java.util.Optional;

public class DeleteConversation implements APIHandler {

    private static final Logger logger = LogManager.getLogger(DeleteConversation.class);
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
            } else {
                Optional<ServerChannel> channel = optionalServer.get().getChannelById(CacheManager.Cache_GetName2Channel(Handle.RequestParams.getString("name")));
                if (channel.isEmpty()) {
                    Response.code = 502;
                    json.put("code", 502);
                    json.put("message", "当前对话不存在");
                    JSONObject json_data = new JSONObject();
                    json_data.put("status", false);
                    json.put("data", json_data);
                } else {
                    channel.get().delete().join();

                    Response.code = 200;
                    json.put("code", 200);
                    json.put("message", "删除对话成功");
                    JSONObject json_data = new JSONObject();
                    json_data.put("status", true);
                    json.put("data", json_data);
                }
            }
        }
        Response.msg = json.toJSONString();
        Response.Header_Content_Type = "application/json; charset=UTF-8";

        return Response;
    }
}
