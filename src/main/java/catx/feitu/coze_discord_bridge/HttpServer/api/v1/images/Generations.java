package catx.feitu.coze_discord_bridge.HttpServer.api.v1.images;

import catx.feitu.coze_discord_bridge.Config.ConfigManage;
import catx.feitu.coze_discord_bridge.HttpServer.APIHandler;
import catx.feitu.coze_discord_bridge.HttpServer.HandleType;
import catx.feitu.coze_discord_bridge.HttpServer.ResponseType;
import catx.feitu.coze_discord_bridge.api.Exceptions.InvalidConversationException;
import catx.feitu.coze_discord_bridge.api.Types.GenerateMessage;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.time.Instant;

public class Generations implements APIHandler {
    @Override
    public ResponseType handle(HandleType Handle) {
        ResponseType Response = new ResponseType();
        JSONObject json = new JSONObject(true);
        try {
            // 创建频道
            try { Handle.CozeGPT.GetConversationInfo(ConfigManage.configs.OpenAPI_ImageGenerate_Default_Channel); }
            catch (InvalidConversationException e) { Handle.CozeGPT.CreateConversation(ConfigManage.configs.OpenAPI_ImageGenerate_Default_Channel); }
            GenerateMessage Generate = Handle.CozeGPT.Chat(
                    ConfigManage.configs.OpenAI_ImageGenerate_Prompt_Prefix +
                            Handle.RequestParams.getString("prompt") +
                            ConfigManage.configs.OpenAI_ImageGenerate_Prompt_Suffix,
                    ConfigManage.configs.OpenAPI_ImageGenerate_Default_Channel
            );
            JSONArray dataArray = new JSONArray();
            json.put("created", Instant.now().toEpochMilli() / 1000);
            for (int i = 0; Generate.Files.size() > i; i++) {
                JSONObject dataItem1 = new JSONObject();
                dataItem1.put("url", Generate.Files.get(i));
                dataArray.add(dataItem1);
            }
            json.put("data", dataArray);
            Response.code = 200;
            Response.msg = json.toJSONString();
            return Response;
        } catch (Exception e) {
            Response.code = 400;
            JSONObject error = new JSONObject();
            error.put("message", e.getClass().getSimpleName());
            error.put("type", "one_api_error");
            json.put("error", error);
            Response.msg = json.toJSONString();
            return Response;
        }
    }
}
