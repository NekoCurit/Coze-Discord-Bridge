package catx.feitu.coze_discord_bridge.HttpServer.api.api;

import catx.feitu.coze_discord_bridge.Misc.CacheManager;
import catx.feitu.coze_discord_bridge.HttpServer.APIHandler;
import catx.feitu.coze_discord_bridge.HttpServer.HandleType;
import catx.feitu.coze_discord_bridge.HttpServer.ResponseType;
import catx.feitu.coze_discord_bridge.api.CozeGPT;
import catx.feitu.coze_discord_bridge.api.Exceptions.InvalidConfigException;
import catx.feitu.coze_discord_bridge.api.Exceptions.InvalidConversationException;
import catx.feitu.coze_discord_bridge.api.Types.ConversationInfo;
import com.alibaba.fastjson.JSONObject;

public class ConversationIsFound implements APIHandler {
    @Override
    public ResponseType handle(HandleType Handle) {
        ResponseType Response = new ResponseType();
        JSONObject json = new JSONObject(true);
        String Name = Handle.RequestParams.containsKey("name") ? Handle.RequestParams.getString("name") : "";
        Name = CacheManager.Cache_GetName2Channel(Name);
        try {
            ConversationInfo Info = CozeGPT.GetConversationInfo(Name);
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
        } catch (InvalidConfigException e) {
            Response.code = 502;
            json.put("code", 502);
            json.put("message", "服务端配置异常:" + e.Get_Invalid_ConfigName() + ":" + e.Get_message());
            JSONObject json_data = new JSONObject(true);
            json_data.put("status", false);
            json.put("data", json_data);
        } catch (Exception e) {
            Response.code = 502;
            json.put("code", 502);
            json.put("message", "删除对话失败");
            JSONObject json_data = new JSONObject(true);
            json_data.put("status", false);
            json.put("data", json_data);
        }
        Response.msg = json.toJSONString();
        return Response;
    }
}
