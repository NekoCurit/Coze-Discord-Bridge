package catx.feitu.coze_discord_bridge.HttpServer.api.api;

import catx.feitu.coze_discord_bridge.HttpServer.APIHandler;
import catx.feitu.coze_discord_bridge.HttpServer.HandleType;
import catx.feitu.coze_discord_bridge.HttpServer.ResponseType;
import catx.feitu.coze_discord_bridge.api.ConversationManage.ConversationHelper;
import catx.feitu.coze_discord_bridge.api.Exceptions.InvalidConfigException;
import catx.feitu.coze_discord_bridge.api.Exceptions.InvalidConversationException;
import com.alibaba.fastjson.JSONObject;

import static catx.feitu.coze_discord_bridge.Misc.Random.RandomName;

public class GetConversations implements APIHandler {

    @Override
    public ResponseType handle(HandleType Handle) {
        ResponseType Response = new ResponseType();
        JSONObject json = new JSONObject(true);
        try {
            Response.code = 200;
            json.put("code", 200);
            json.put("message", "获取对话列表成功");
            json.put("data", Handle.CozeGPT.conversations.conversations);
        } catch (Exception e) {
            Response.code = 502;
            json.put("code", 502);
            json.put("message", "获取对话列表失败");
            JSONObject json_data = new JSONObject(true);
            json.put("data", json_data);
        }
        Response.msg = json.toJSONString();
        return Response;
    }
}
