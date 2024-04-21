package catx.feitu.coze_discord_bridge.HttpServer.api.api;

import catx.feitu.coze_discord_bridge.HttpServer.APIHandler;
import catx.feitu.coze_discord_bridge.HttpServer.HandleType;
import catx.feitu.coze_discord_bridge.HttpServer.ResponseType;
import com.alibaba.fastjson.JSONObject;

import static catx.feitu.coze_discord_bridge.utils.RandomUtils.RandomName;

public class DeleteConversation implements APIHandler {

    @Override
    public ResponseType handle(HandleType Handle) {
        ResponseType Response = new ResponseType();
        JSONObject json = new JSONObject(true);

        String Name = Handle.RequestParams.containsKey("name") ? Handle.RequestParams.getString("name") : RandomName();
        try {
            Handle.CozeGPT.deleteConversation(Name);

            Response.code = 200;
            json.put("code", 200);
            json.put("message", "删除对话成功");
            JSONObject json_data = new JSONObject(true);
            json_data.put("status", true);
            json.put("data", json_data);
        } catch (Exception e) {
            Response.code = 400;
            json.put("code", 400);
            json.put("message", "删除对话失败:" + e.getClass().getSimpleName() + ":" + e.getMessage());
            json.put("data", null);
        }
        Response.msg = json.toJSONString();
        return Response;
    }
}
