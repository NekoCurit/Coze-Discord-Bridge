package catx.feitu.coze_discord_bridge.HttpServer.api.api.Protocol;

import catx.feitu.coze_discord_bridge.HttpServer.APIHandler;
import catx.feitu.coze_discord_bridge.HttpServer.HandleType;
import catx.feitu.coze_discord_bridge.HttpServer.ResponseType;
import com.alibaba.fastjson.JSONObject;

public class ReLogin implements APIHandler {
    @Override
    public ResponseType handle(HandleType Handle) {
        ResponseType Response = new ResponseType();
        JSONObject json = new JSONObject(true);
        try {
            Handle.CozeGPT.login();
            Response.code = 200;
            json.put("code", 200);
            json.put("message", "重新登录成功");
            JSONObject json_data = new JSONObject(true);
            json_data.put("status", true);
            json.put("data", json_data);
        } catch (Exception e) {
            Response.code = 502;
            json.put("code", 502);
            json.put("message", "重新登录失败");
            JSONObject json_data = new JSONObject(true);
            json_data.put("status", false);
            json.put("data", json_data);
        }
        Response.msg = json.toJSONString();
        return Response;
    }
}
