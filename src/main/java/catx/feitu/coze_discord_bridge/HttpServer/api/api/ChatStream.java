package catx.feitu.coze_discord_bridge.HttpServer.api.api;

import catx.feitu.CozeProxy.Types.GPTFile;
import catx.feitu.CozeProxy.Types.GenerateMessage;
import catx.feitu.coze_discord_bridge.HttpServer.APIHandler;
import catx.feitu.coze_discord_bridge.HttpServer.HandleType;
import catx.feitu.coze_discord_bridge.HttpServer.ResponseType;
import com.alibaba.fastjson.JSONObject;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class ChatStream implements APIHandler {
    @Override
    public ResponseType handle(HandleType Handle) {
        ResponseType Response = new ResponseType();
        JSONObject json = new JSONObject(true);
        String Prompt = Handle.RequestParams.containsKey("prompt") ? Handle.RequestParams.getString("prompt") : "";
        String Name = Handle.RequestParams.containsKey("name") ? Handle.RequestParams.getString("name") : "";
        List<GPTFile> Files = new ArrayList<>();
        try {
            if (Handle.RequestParams.containsKey("image")) {
                Files.add(new GPTFile(Handle.RequestParams.getString("image"),"default.png"));
            }

            OutputStream os = Handle.HttpExchange.getResponseBody();
            Handle.HttpExchange_Disable_Default_Action = true;
            Handle.HttpExchange.getResponseHeaders().add("Transfer-Encoding", "chunked");
            Handle.HttpExchange.sendResponseHeaders(200, 0);

            GenerateMessage Generate = Handle.CozeGPT.chat(Prompt, Name, Files ,(ALLGenerateMessages, NewGenerateMessage) -> {
                json.put("code", 200);
                json.put("message", "生成中..");
                JSONObject json_data = new JSONObject(true);
                json_data.put("prompt_all", ALLGenerateMessages);
                json_data.put("prompt_new", NewGenerateMessage);
                json_data.put("files", null);
                json_data.put("done", false);
                json.put("data", json_data);
                try {
                    os.write(("data: " + json.toJSONString() + "\n\n").getBytes(StandardCharsets.UTF_8));
                    os.flush();
                    return true;
                } catch (Exception ignored) {
                    return false;
                }
            });
            Response.code = 200;
            json.put("code", 200);
            json.put("message", "成功!");
            JSONObject json_data = new JSONObject(true);
            json_data.put("prompt", Generate.Message);
            json_data.put("files", Generate.Files);
            json.put("data", json_data);
            os.write(("data: " + json.toJSONString() + "\n\n").getBytes(StandardCharsets.UTF_8));
            os.flush();
            os.close();
            return null;
        } catch (Exception e) {
            Response.code = 400;
            json.put("code", 400);
            json.put("message", e.getClass().getSimpleName() + ":" + e.getMessage());
            JSONObject json_data = new JSONObject(true);
            json_data.put("status", false);
            json.put("data", json_data);
        }
        Response.msg = json.toJSONString();
        Handle.HttpExchange_Disable_Default_Action = false;
        return Response;
    }
}
