package catx.feitu.coze_discord_bridge.HttpServer.api.api;

import catx.feitu.coze_discord_bridge.HttpServer.APIHandler;
import catx.feitu.coze_discord_bridge.HttpServer.HandleType;
import catx.feitu.coze_discord_bridge.HttpServer.ResponseType;
import com.alibaba.fastjson.JSONObject;

import static catx.feitu.coze_discord_bridge.utils.RandomUtils.RandomName;

public class CreateConversation implements APIHandler {

    @Override
    public ResponseType handle(HandleType Handle) {
        ResponseType Response = new ResponseType();
        JSONObject json = new JSONObject(true);

        String name = Handle.RequestParams.containsKey("name") ? Handle.RequestParams.getString("name") : RandomName();
        try {
            String ChannelID = Handle.CozeGPT.createConversation(name);

            Response.code = 200;
            JSONObject json_data = new JSONObject(true);
            json.put("code", 200);
            json.put("message", "创建子频道成功!");
            json_data.put("conversation_id", ChannelID);
            json_data.put("conversation_name", name);
            json.put("data", json_data);
        } catch (Exception e) {
            Response.code = 400;
            json.put("code", 400);
            json.put("message", "创建对话失败:" + e.getClass().getSimpleName() + ":" + e.getMessage());
            json.put("data", null);
        }
        Response.msg = json.toJSONString();
        return Response;
    }
}
