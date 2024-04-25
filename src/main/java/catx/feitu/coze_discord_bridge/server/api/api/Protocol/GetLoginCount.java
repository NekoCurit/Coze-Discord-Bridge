package catx.feitu.coze_discord_bridge.server.api.api.Protocol;

import catx.feitu.coze_discord_bridge.server.APIHandler;
import catx.feitu.coze_discord_bridge.server.HandleType;
import catx.feitu.coze_discord_bridge.server.ResponseType;
import com.alibaba.fastjson.JSONObject;

public class GetLoginCount implements APIHandler {
    @Override
    public ResponseType handle(HandleType Handle) {
        ResponseType Response = new ResponseType();
        JSONObject json = new JSONObject(true);

        Response.code = 200;
        json.put("code", 200);
        json.put("message", "获取成功");
        JSONObject json_data = new JSONObject(true);
        json_data.put("count", Handle.CozeGPT.getLoginCount());
        json.put("data", json_data);

        Response.msg = json.toJSONString();
        return Response;
    }
}
