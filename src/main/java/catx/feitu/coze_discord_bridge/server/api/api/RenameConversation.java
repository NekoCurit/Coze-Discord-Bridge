package catx.feitu.coze_discord_bridge.server.api.api;

import catx.feitu.coze_discord_bridge.server.APIHandler;
import catx.feitu.coze_discord_bridge.server.HandleType;
import catx.feitu.coze_discord_bridge.server.ResponseType;
import com.alibaba.fastjson.JSONObject;

public class RenameConversation implements APIHandler {
    @Override
    public ResponseType handle(HandleType Handle) {
        ResponseType Response = new ResponseType();
        JSONObject json = new JSONObject(true);

        String OldName = Handle.RequestParams.containsKey("name") ? Handle.RequestParams.getString("name") : "";
        String NewName = Handle.RequestParams.containsKey("new_name") ? Handle.RequestParams.getString("new_name") : "";
        try {
            String ChannelID = Handle.CozeGPT.renameConversation(OldName,NewName);

            Response.code = 200;
            json.put("code", 200);
            json.put("message", "修改名称成功");
            JSONObject json_data = new JSONObject(true);
            json_data.put("status", true);
            json_data.put("conversation_id", ChannelID);
            json_data.put("conversation_name", NewName);
            json.put("data", json_data);
        } catch (Exception e) {
            Response.code = 400;
            json.put("code", 400);
            json.put("message", "重命名对话失败:" + e.getClass().getSimpleName() + ":" + e.getMessage());
            json.put("data", null);
        }
        Response.msg = json.toJSONString();
        return Response;
    }
}
