package catx.feitu.coze_discord_bridge.server.api.api;

import catx.feitu.CozeProxy.exception.InvalidConversationException;
import catx.feitu.CozeProxy.impl.ConversationInfo;
import catx.feitu.coze_discord_bridge.server.APIHandler;
import catx.feitu.coze_discord_bridge.server.HandleType;
import catx.feitu.coze_discord_bridge.server.ResponseType;
import com.alibaba.fastjson.JSONObject;

public class ConversationIsFound implements APIHandler {
    @Override
    public ResponseType handle(HandleType Handle) {
        ResponseType Response = new ResponseType();
        JSONObject json = new JSONObject(true);
        String Name = Handle.RequestParams.containsKey("name") ? Handle.RequestParams.getString("name") : "";
        try {
            ConversationInfo Info = Handle.CozeGPT.getConversationInfo(Name);
            Response.code = 200;
            json.put("code", 200);
            json.put("message", "当前对话存在");
            JSONObject json_data = new JSONObject(true);
            json_data.put("status", true);
            json_data.put("conversation_name", Info.Name);
            json_data.put("conversation_id", Info.ID);
            json.put("data", json_data);
        } catch (InvalidConversationException e) {
            Response.code = 200;
            json.put("code", 200);
            json.put("message", "当前对话不存在");
            JSONObject json_data = new JSONObject(true);
            json_data.put("status", false);
            json.put("data", json_data);
        } catch (Exception e) {
            Response.code = 400;
            json.put("code", 400);
            json.put("message", "获取失败:" + e.getClass().getSimpleName() + ":" + e.getMessage());
            JSONObject json_data = new JSONObject(true);
            json_data.put("status", false);
            json.put("data", json_data);
        }
        Response.msg = json.toJSONString();
        return Response;
    }
}
