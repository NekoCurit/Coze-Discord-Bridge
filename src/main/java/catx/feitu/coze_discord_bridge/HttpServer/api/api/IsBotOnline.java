package catx.feitu.coze_discord_bridge.HttpServer.api.api;

import catx.feitu.coze_discord_bridge.HttpServer.APIHandler;
import catx.feitu.coze_discord_bridge.HttpServer.HandleType;
import catx.feitu.coze_discord_bridge.HttpServer.ResponseType;
import com.alibaba.fastjson.JSONObject;

public class IsBotOnline implements APIHandler {
    @Override
    public ResponseType handle(HandleType Handle) {
        ResponseType Response = new ResponseType();
        JSONObject json = new JSONObject(true);
        try {
           Response.code = 200;
            json.put("code", 200);
            json.put("message", "获取成功");
            JSONObject json_data = new JSONObject(true);
            json_data.put("status", Handle.CozeGPT.IsCozeBotOnline());
            json.put("data", json_data);
        } catch (Exception e) {
            Response.code = 502;
            json.put("code", 502);
            json.put("message", "获取失败");
            JSONObject json_data = new JSONObject(true);
            json.put("data", json_data);
        }
        Response.msg = json.toJSONString();
        return Response;
    }
}
