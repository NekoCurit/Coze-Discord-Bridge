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
import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.server.Server;

import java.util.Optional;
import java.util.Random;

public class CreateConversation implements APIHandler {

    private static final Logger logger = LogManager.getLogger(CreateConversation.class);
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
                Handle.RequestParams.put("name",RandomName());
            }
            try {
                ServerTextChannel channel = optionalServer.get().createTextChannelBuilder()
                        .setName(Handle.RequestParams.getString("name"))
                        .create()
                        .join();
                JSONObject json_data = new JSONObject();
                Response.code = 200;
                json.put("code", 200);
                json.put("message", "创建子频道成功!");
                json_data.put("conversation_id", channel.getIdAsString());
                json_data.put("conversation_name", channel.getName());
                CacheManager.Cache_AddName2Channel(channel.getName(),channel.getIdAsString());
                json.put("data", json_data);
            } catch (Exception exception) {
                Response.code = 502;
                json.put("code", 502);
                json.put("message", "与Discord服务端交流异常:创建频道失败");
                json.put("data", null);
                logger.warn("执行CreateConversation失败:与Discord服务端交流异常:创建频道失败", exception);
            }
        }
        logger.warn(5);
        logger.warn(json.toJSONString());

        Response.msg = json.toJSONString();

        return Response;
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
