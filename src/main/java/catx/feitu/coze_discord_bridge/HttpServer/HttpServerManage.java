package catx.feitu.coze_discord_bridge.HttpServer;

import catx.feitu.coze_discord_bridge.Config.ConfigManage;
import catx.feitu.coze_discord_bridge.HttpServer.api.Ping;
import catx.feitu.coze_discord_bridge.HttpServer.api.api.*;
import catx.feitu.coze_discord_bridge.HttpServer.api.index;
import com.alibaba.fastjson.JSONObject;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static catx.feitu.coze_discord_bridge.HttpServer.HttpServerManage.APIS;

public class HttpServerManage {

    public static HttpServer server;

    static Map<String, APIHandler> APIS = new HashMap<>();

    public static void Start (int Port) throws IOException {
        server = HttpServer.create(new InetSocketAddress(Port), 0);
        server.createContext("/", new HttpHandle());

        server.start();
        AddAPI("/",new index());
        AddAPI("/api/CreateConversation", new CreateConversation());
        AddAPI("/api/DeleteConversation", new DeleteConversation());
        AddAPI("/api/ConversationIsFound", new ConversationIsFound());
        AddAPI("/api/Chat", new Chat());
        AddAPI("/Ping", new Ping());


    }

    public static void AddAPI (String s ,APIHandler api) {
        APIS.put(s.toLowerCase(), api);
    }
}
class HttpHandle implements HttpHandler {
    private static final Logger logger = LogManager.getLogger(HttpServerManage.class);
    @Override
    public void handle(HttpExchange t) throws IOException {
        ResponseType Response = new ResponseType();
        logger.info(t.getRequestURI().getPath());
        APIHandler handler = APIS.get(t.getRequestURI().getPath().toLowerCase());
        if (handler == null) { // 404 Not Found
            JSONObject json = new JSONObject();
            json.put("code", 404);
            json.put("message", "终结点不存在");
            Response.msg = json.toJSONString();
            Response.code = 404;
            Response.Header_Content_Type = "application/json; charset=UTF-8";
        } else { // 200 Successful
            String query = "GET".equals(t.getRequestMethod()) ?
                    t.getRequestURI().getQuery() :
                    new BufferedReader(new InputStreamReader(t.getRequestBody(), StandardCharsets.UTF_8)).readLine();
            HandleType handle = new HandleType();
            handle.RequestParams = new JSONObject();

            if (query != null) {
                for (String param : query.split("&")) {
                    String[] keyValue = param.split("=");
                    if (keyValue.length > 1) {
                        handle.RequestParams.put(URLDecoder.decode(keyValue[0], StandardCharsets.UTF_8), URLDecoder.decode(keyValue[1], StandardCharsets.UTF_8));
                    } else if (keyValue.length == 1) {
                        handle.RequestParams.put(URLDecoder.decode(keyValue[0], StandardCharsets.UTF_8), "");
                    }
                }
            }

                boolean Verifyed = Objects.equals(ConfigManage.Configs.APIKey, "");
                if (handle.RequestParams.containsKey("key")) {
                    Verifyed = Verifyed || Objects.equals(handle.RequestParams.getString("key"), ConfigManage.Configs.APIKey);
                }
                Verifyed = Verifyed || Objects.equals(t.getRequestHeaders().getFirst("key"), ConfigManage.Configs.APIKey);
                if (!Verifyed) {
                    JSONObject json = new JSONObject();
                    json.put("code", 403);
                    json.put("message", "无权访问本服务");
                    Response.msg = json.toJSONString();
                    Response.code = 403;
                    Response.Header_Content_Type = "application/json; charset=UTF-8";

                }
                else {
                    handle.RequestPath = t.getRequestURI().getPath();
                    Response = handler.handle(handle);
                }

        }



        t.getResponseHeaders().set("Content-Type", Response.Header_Content_Type);
        t.sendResponseHeaders(Response.code, Response.msg.getBytes(StandardCharsets.UTF_8).length);
        OutputStream os = t.getResponseBody();
        os.write(Response.msg.getBytes());
        os.close();
    }
}