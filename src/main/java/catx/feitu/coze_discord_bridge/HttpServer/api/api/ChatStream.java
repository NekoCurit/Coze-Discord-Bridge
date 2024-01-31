package catx.feitu.coze_discord_bridge.HttpServer.api.api;

import catx.feitu.coze_discord_bridge.Config.ConfigManage;
import catx.feitu.coze_discord_bridge.Discord.Discord;
import catx.feitu.coze_discord_bridge.HttpServer.APIHandler;
import catx.feitu.coze_discord_bridge.HttpServer.HandleType;
import catx.feitu.coze_discord_bridge.HttpServer.ResponseType;
import catx.feitu.coze_discord_bridge.Misc.BotReplyType;
import catx.feitu.coze_discord_bridge.Misc.CacheManager;
import catx.feitu.coze_discord_bridge.Misc.LockManager;
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
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class ChatStream implements APIHandler {

    private static final Logger logger = LogManager.getLogger(ChatStream.class);
    @Override
    public ResponseType handle(HandleType Handle) {
        ResponseType Response = new ResponseType();
        JSONObject json = new JSONObject(true);
        Optional<Server> optionalServer = Discord.api.getServerById(ConfigManage.Configs.CozeBot_InServer_id);
        if (optionalServer.isEmpty()) {
            Response.code = 502;
            json.put("code", 502);
            json.put("message", "服务端配置异常:CozeBot_InServer_id没有找到匹配的Discord服务器");
            logger.warn("执行CreateConversation失败:服务端配置异常:CozeBot_InServer_id没有找到匹配的Discord服务器");
        }
        else {
            if (!Handle.RequestParams.containsKey("name")) {
                Response.code = 400;
                json.put("code", 400);
                json.put("message", "参数缺失:name");
                JSONObject json_data = new JSONObject(true);
                json.put("data", json_data);
            }
            if (!Handle.RequestParams.containsKey("prompt")) {
                Response.code = 400;
                json.put("code", 400);
                json.put("message", "参数缺失:prompt");
                JSONObject json_data = new JSONObject(true);
                json.put("data", json_data);
            }
            else {
                Optional<ServerChannel> channel = optionalServer.get().getChannelById(CacheManager.Cache_GetName2Channel(Handle.RequestParams.getString("name")));
                if (channel.isEmpty()) {
                    Response.code = 502;
                    json.put("code", 502);
                    json.put("message", "当前对话不存在");
                    JSONObject json_data = new JSONObject(true);
                    json_data.put("status", false);
                    json.put("data", json_data);
                } else {
                    if(channel.get() instanceof TextChannel) {
                        TextChannel textChannel = (TextChannel) channel.get();
                        // IDEA会提示 「 变量 'textChannel' 可被替换为模式变量 」  不要点 点了就build failed
                        // 误点补救处 复制替换上去即可
                        // if(channel.get() instanceof TextChannel) {
                        // TextChannel textChannel = (TextChannel) channel.get();
                        LockManager.getLock(channel.get().getIdAsString()).lock(); // 上锁
                        CacheManager.Cache_BotReplyClear(textChannel.getIdAsString());

                        CompletableFuture<Message> send = textChannel.sendMessage("<@" + ConfigManage.Configs.CozeBot_id + ">" + Handle.RequestParams.getString("prompt"));
                        if (Handle.RequestParams.containsKey("image")) {
                            String encodedImage = Handle.RequestParams.getString("image")
                                    .replace("data:image/png;base64,","");
                            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                            try {
                                // 使用 Java 自带的Base64解码器解码字符串
                                byte[] imageBytes = Base64.getDecoder().decode(encodedImage);
                                BufferedImage image = ImageIO.read(new ByteArrayInputStream(imageBytes));
                                if (image == null) {
                                    throw new IllegalArgumentException("返回图片为空");
                                }
                                ImageIO.write(image, "png", outputStream);
                                try (InputStream sendStream = new ByteArrayInputStream(outputStream.toByteArray())) {
                                    EmbedBuilder embed = new EmbedBuilder().setImage(sendStream);
                                    send = send.thenCompose(message -> textChannel.sendMessage(embed));
                                }
                            } catch (Exception e) {
                                logger.warn("解析图片失败\r\n" + encodedImage, e);
                            }
                        }
                        send.join();
                        BotReplyType Reply = new BotReplyType();
                        try (OutputStream os = Handle.HttpExchange.getResponseBody()) {
                            Handle.HttpExchange_Disable_Default_Action = true;
                            Handle.HttpExchange.getResponseHeaders().add("Transfer-Encoding", "chunked");
                            Handle.HttpExchange.sendResponseHeaders(200, 0);
                            // 开发者留言: 长度一定要是0 不能是-1 因为-1白白折腾了半个小时喵..

                            int retryInterval = 200; // 等待时间间隔 单位为毫秒
                            int maxRetries = 300; // 最大尝试次数
                            int attempt = 0; // 当前尝试次数

                            String OldMessage = "";

                            while (!Reply.Done) {
                                attempt++; // +1
                                Reply = CacheManager.Cache_BotReplyMessageEx(textChannel.getIdAsString());
                                if (!OldMessage.equals(Reply.TextMessage)) {
                                    json.put("code", 200);
                                    json.put("message", "生成中..");
                                    JSONObject json_data = new JSONObject(true);
                                    json_data.put("prompt_all", Reply.TextMessage);
                                    json_data.put("prompt_new", Reply.TextMessage.replace(OldMessage,""));
                                    json_data.put("files", Reply.Files);
                                    json_data.put("done", false);
                                    json.put("data", json_data);

                                    os.write(("data: " + json.toJSONString() + "\n\n").getBytes(StandardCharsets.UTF_8));
                                    os.flush();
                                    OldMessage = Reply.TextMessage;
                                }
                                if (attempt < maxRetries) {
                                    try { Thread.sleep(retryInterval); } catch (InterruptedException ignored) {}
                                } else {
                                    break;
                                }
                            }
                            LockManager.getLock(channel.get().getIdAsString()).unlock(); // 解锁
                            Response.code = 200;
                            json.put("code", 200);
                            json.put("message", "成功!");
                            JSONObject json_data = new JSONObject(true);
                            json_data.put("prompt_all", Reply.TextMessage);
                            json_data.put("prompt_new", Reply.TextMessage.replace(OldMessage,""));
                            json_data.put("files", Reply.Files);
                            json_data.put("done", true);
                            json.put("data", json_data);
                            os.write(("data: " + json.toJSONString() + "\n\n").getBytes(StandardCharsets.UTF_8));
                            os.flush();
                            Handle.HttpExchange.sendResponseHeaders(200, 0);
                            Handle.HttpExchange.close();
                            return Response;
                        } catch (Exception ignored) { }
                    } else {
                        Response.code = 502;
                        json.put("code", 502);
                        json.put("message", "执行失败:目标非文本频道");
                        JSONObject json_data = new JSONObject(true);
                        json.put("data", json_data);
                    }
                }
            }
        }
        Response.msg = json.toJSONString();

        return Response;
    }
}
