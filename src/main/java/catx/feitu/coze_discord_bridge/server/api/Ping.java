package catx.feitu.coze_discord_bridge.server.api;

import catx.feitu.coze_discord_bridge.server.APIHandler;
import catx.feitu.coze_discord_bridge.server.HandleType;
import catx.feitu.coze_discord_bridge.server.ResponseType;
import catx.feitu.coze_discord_bridge.server.api.api.GetLatestMessage;
import com.alibaba.fastjson.JSONObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class Ping implements APIHandler {

    private static final Logger logger = LogManager.getLogger(GetLatestMessage.class);
    @Override
    public ResponseType handle(HandleType Handle) {
        ResponseType Response = new ResponseType();
        JSONObject json = new JSONObject(true);
        json.put("code", 200);
        json.put("message", "Pong!");

        Response.msg = json.toJSONString();

        return Response;
    }
}
