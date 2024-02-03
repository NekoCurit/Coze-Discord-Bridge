package catx.feitu.coze_discord_bridge.HttpServer.api.api;

import catx.feitu.coze_discord_bridge.HttpServer.APIHandler;
import catx.feitu.coze_discord_bridge.HttpServer.HandleType;
import catx.feitu.coze_discord_bridge.HttpServer.ResponseType;
import catx.feitu.coze_discord_bridge.api.Exceptions.ConversationAlreadyExistsException;
import catx.feitu.coze_discord_bridge.api.Exceptions.InvalidConfigException;
import com.alibaba.fastjson.JSONObject;

import static catx.feitu.coze_discord_bridge.Misc.Random.RandomName;

public class CreateConversation implements APIHandler {

    @Override
    public ResponseType handle(HandleType Handle) {
        ResponseType Response = new ResponseType();
        JSONObject json = new JSONObject(true);

        String Name = Handle.RequestParams.containsKey("name") ? Handle.RequestParams.getString("name") : RandomName();
        try {
            String ChannelID = Handle.CozeGPT.CreateConversation(Name);

            Response.code = 200;
            JSONObject json_data = new JSONObject(true);
            json.put("code", 200);
            json.put("message", "创建子频道成功!");
            json_data.put("conversation_id", ChannelID);
            json_data.put("conversation_name", Name);
            json.put("data", json_data);
        } catch (InvalidConfigException e) {
            Response.code = 502;
            json.put("code", 502);
            json.put("message", "服务端配置异常:" + e.Get_Invalid_ConfigName() + ":" + e.Get_message());
            JSONObject json_data = new JSONObject(true);
            json_data.put("status", false);
            json.put("data", json_data);
        } catch (ConversationAlreadyExistsException e) {
            Response.code = 502;
            json.put("code", 502);
            json.put("message", "对话已存在");
            json.put("data", null);
        } catch (Exception e) {
            Response.code = 502;
            json.put("code", 502);
            json.put("message", "创建对话失败");
            json.put("data", null);
            e.printStackTrace();
        }
        Response.msg = json.toJSONString();
        return Response;
    }
}
