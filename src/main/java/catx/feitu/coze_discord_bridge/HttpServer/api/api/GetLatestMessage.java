package catx.feitu.coze_discord_bridge.HttpServer.api.api;

import catx.feitu.coze_discord_bridge.HttpServer.APIHandler;
import catx.feitu.coze_discord_bridge.HttpServer.HandleType;
import catx.feitu.coze_discord_bridge.HttpServer.ResponseType;
import catx.feitu.coze_discord_bridge.api.MessageManage.BotResponseType;
import com.alibaba.fastjson.JSONObject;

public class GetLatestMessage implements APIHandler {

    @Override
    public ResponseType handle(HandleType Handle) {
        ResponseType Response = new ResponseType();
        JSONObject json = new JSONObject(true);
        String Name = Handle.RequestParams.containsKey("name") ? Handle.RequestParams.getString("name") : "";
        try {
            BotResponseType Generate = Handle.CozeGPT.BotResponseManage.getMsg(Handle.CozeGPT.conversations.get(Name));

            json.put("code", 200);
            json.put("message", "成功!");
            JSONObject json_data = new JSONObject(true);
            json_data.put("prompt", Generate.prompt);
            json_data.put("files", Generate.files);
            json.put("data", json_data);
        } catch (Exception ignored) {
            Response.code = 502;
            json.put("code", 502);
            json.put("message", "当前对话不存在");
            JSONObject json_data = new JSONObject(true);
            json.put("data", json_data);
        }        Response.msg = json.toJSONString();
        return Response;
    }
}
