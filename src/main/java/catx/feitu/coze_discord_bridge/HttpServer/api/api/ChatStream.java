package catx.feitu.coze_discord_bridge.HttpServer.api.api;

import catx.feitu.coze_discord_bridge.Config.ConfigManage;
import catx.feitu.coze_discord_bridge.Discord.Discord;
import catx.feitu.coze_discord_bridge.HttpServer.APIHandler;
import catx.feitu.coze_discord_bridge.HttpServer.HandleType;
import catx.feitu.coze_discord_bridge.HttpServer.ResponseType;
import catx.feitu.coze_discord_bridge.Misc.BotReplyType;
import catx.feitu.coze_discord_bridge.Misc.CacheManager;
import catx.feitu.coze_discord_bridge.Misc.LockManager;
import catx.feitu.coze_discord_bridge.Misc.TempFileManger;
import catx.feitu.coze_discord_bridge.api.CozeGPT;
import catx.feitu.coze_discord_bridge.api.Exceptions.InvalidPromptException;
import catx.feitu.coze_discord_bridge.api.Exceptions.PromptTooLongException;
import catx.feitu.coze_discord_bridge.api.Exceptions.RecvMsgException;
import catx.feitu.coze_discord_bridge.api.Types.GenerateMessage;
import com.alibaba.fastjson.JSONObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.javacord.api.entity.channel.ServerChannel;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.server.Server;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CompletableFuture;

import static catx.feitu.coze_discord_bridge.Misc.Random.RandomName;

public class ChatStream implements APIHandler {

    private static final Logger logger = LogManager.getLogger(ChatStream.class);
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

            OutputStream os = Handle.HttpExchange.getResponseBody();
            Handle.HttpExchange_Disable_Default_Action = true;
            Handle.HttpExchange.getResponseHeaders().add("Transfer-Encoding", "chunked");
            Handle.HttpExchange.sendResponseHeaders(200, 0);

            GenerateMessage Generate = CozeGPT.Chat(Prompt, Name, Files,(ALLGenerateMessages, NewGenerateMessage) -> {
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
