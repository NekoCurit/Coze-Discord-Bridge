package catx.feitu.coze_discord_bridge.server.api.api;

import catx.feitu.coze_discord_bridge.GPTManage;
import catx.feitu.coze_discord_bridge.server.APIHandler;
import catx.feitu.coze_discord_bridge.server.HandleType;
import catx.feitu.coze_discord_bridge.server.ResponseType;
import com.alibaba.fastjson.JSONObject;

public class Keepalive implements APIHandler {
    @Override
    public ResponseType handle(HandleType Handle) {
        ResponseType Response = new ResponseType();
        JSONObject json = new JSONObject(true);
        GPTManage.keepalive();
        Response.code = 200;
        json.put("code", 200);
        json.put("message", "keepalive执行成功");
        Response.msg = json.toJSONString();
        return Response;
    }
}
