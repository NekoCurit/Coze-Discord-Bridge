package catx.feitu.coze_discord_bridge.server.api.v1;

import catx.feitu.coze_discord_bridge.server.APIHandler;
import catx.feitu.coze_discord_bridge.server.HandleType;
import catx.feitu.coze_discord_bridge.server.ResponseType;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

public class Models implements APIHandler {

    @Override
    public ResponseType handle(HandleType Handle) {
        ResponseType Response = new ResponseType();

        JSONObject json = new JSONObject(true);
        json.put("object", "list");

        JSONArray jsonArray = new JSONArray();

        JSONObject model = new JSONObject();
        model.put("id", "gpt-3.5-turbo-0301");
        model.put("object", "model");
        model.put("created", 1677649963);
        model.put("owned_by", "openai");

        jsonArray.add(model);
        json.put("data", jsonArray);

        Response.msg = json.toJSONString();
        Response.code = 200;

        return Response;
    }
}
