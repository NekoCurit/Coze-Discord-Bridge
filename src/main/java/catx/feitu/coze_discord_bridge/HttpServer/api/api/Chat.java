package catx.feitu.coze_discord_bridge.HttpServer.api.api;

import catx.feitu.coze_discord_bridge.Misc.CacheManager;
import catx.feitu.coze_discord_bridge.HttpServer.APIHandler;
import catx.feitu.coze_discord_bridge.HttpServer.HandleType;
import catx.feitu.coze_discord_bridge.HttpServer.ResponseType;
import catx.feitu.coze_discord_bridge.Misc.TempFileManger;
import catx.feitu.coze_discord_bridge.api.CozeGPT;
import catx.feitu.coze_discord_bridge.api.Exceptions.InvalidPromptException;
import catx.feitu.coze_discord_bridge.api.Exceptions.PromptTooLongException;
import catx.feitu.coze_discord_bridge.api.Exceptions.RecvMsgException;
import catx.feitu.coze_discord_bridge.api.Types.GenerateMessage;
import com.alibaba.fastjson.JSONObject;

import java.io.*;
import java.util.*;

public class Chat implements APIHandler {
    @Override
    public ResponseType handle(HandleType Handle) {
        ResponseType Response = new ResponseType();
        JSONObject json = new JSONObject(true);
        String Prompt = Handle.RequestParams.containsKey("prompt") ? Handle.RequestParams.getString("prompt") : "";
        String Name = Handle.RequestParams.containsKey("name") ? Handle.RequestParams.getString("name") : "";
        List<File> Files = new ArrayList<>();
        try {
            if (Handle.RequestParams.containsKey("image")) {
                Files.add(TempFileManger.fwrite_base64(Handle.RequestParams.getString("image")));
            }
            Name = CacheManager.Cache_GetName2Channel(Name);
            GenerateMessage Generate = CozeGPT.Chat(Prompt, Name, Files);

            Response.code = 200;
            json.put("code", 200);
            json.put("message", "成功!");
            JSONObject json_data = new JSONObject(true);
            json_data.put("prompt", Generate.Message);
            json_data.put("files", Generate.Files);
            json.put("data", json_data);
            Response.msg = json.toJSONString();
            return Response;
        } catch (InvalidPromptException e) {
            Response.code = 400;
            json.put("code", 400);
            json.put("message", "无效的提示词");
            JSONObject json_data = new JSONObject(true);
            json_data.put("status", false);
            json.put("data", json_data);
            Response.msg = json.toJSONString();
            return Response;
        } catch (PromptTooLongException e) {
            Response.code = 400;
            json.put("code", 400);
            json.put("message", "提示词超过长度限制  当前长度:" + e.GetPromptLength() +
                    " > 限制长度:" + e.GetLimitLength());
            JSONObject json_data = new JSONObject(true);
            json_data.put("status", false);
            json.put("data", json_data);
            Response.msg = json.toJSONString();
            return Response;
        } catch (RecvMsgException e) {
            Response.code = 400;
            json.put("code", 400);
            json.put("message", e.getMessage());
            JSONObject json_data = new JSONObject(true);
            json_data.put("status", false);
            json.put("data", json_data);
            Response.msg = json.toJSONString();
            return Response;
        } catch (Exception e) {
            Response.code = 400;
            json.put("code", 400);
            json.put("message", "未知错误");
            JSONObject json_data = new JSONObject(true);
            json_data.put("status", false);
            json.put("data", json_data);
            Response.msg = json.toJSONString();
            return Response;
        }
    }
}
