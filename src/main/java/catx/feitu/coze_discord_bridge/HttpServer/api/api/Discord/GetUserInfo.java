package catx.feitu.coze_discord_bridge.HttpServer.api.api.Discord;

import catx.feitu.coze_discord_bridge.HttpServer.APIHandler;
import catx.feitu.coze_discord_bridge.HttpServer.HandleType;
import catx.feitu.coze_discord_bridge.HttpServer.ResponseType;
import com.alibaba.fastjson.JSONObject;

public class GetUserInfo implements APIHandler {
    @Override
    public ResponseType handle(HandleType Handle) {
        ResponseType Response = new ResponseType();
        JSONObject json = new JSONObject(true);
        try {
            Response.code = 200;
            json.put("code", 200);
            json.put("message", "获取成功");
            JSONObject json_data = new JSONObject(true);
            json_data.put("id", Handle.CozeGPT.discord_api.getYourself().getIdAsString());
            json_data.put("name", Handle.CozeGPT.discord_api.getYourself().getName());
            json_data.put("status", Handle.CozeGPT.discord_api.getYourself().getStatus().getStatusString());
            json.put("data", json_data);
        } catch (Exception e) {
            Response.code = 502;
            json.put("code", 502);
            json.put("message", "获取信息失败");
            JSONObject json_data = new JSONObject(true);
            json.put("data", json_data);
        }
        Response.msg = json.toJSONString();
        return Response;
    }
}
