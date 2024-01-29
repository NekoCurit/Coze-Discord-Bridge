package catx.feitu.coze_discord_bridge.HttpServer;

import catx.feitu.coze_discord_bridge.Config.ConfigManage;
import catx.feitu.coze_discord_bridge.HttpServer.api.Ping;
import catx.feitu.coze_discord_bridge.HttpServer.api.api.*;
import catx.feitu.coze_discord_bridge.HttpServer.api.index;
import catx.feitu.coze_discord_bridge.HttpServer.api.robots;
import catx.feitu.coze_discord_bridge.HttpServer.api.v1.chat.Completions;
import com.alibaba.fastjson.JSONObject;
import com.sun.net.httpserver.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.TrustManagerFactory;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static catx.feitu.coze_discord_bridge.HttpServer.HttpServerManage.APIS;

public class HttpServerManage {

    public static HttpServer server;
    public static HttpsServer server_https;

    static Map<String, APIHandler> APIS = new HashMap<>();

    private static final Logger logger = LogManager.getLogger(HttpServerManage.class);

    public static void Start () throws Exception {
        Boolean SuccessOne = false;
        try {
            server = HttpServer.create(new InetSocketAddress(ConfigManage.Configs.APIPort), 0);
            server.createContext("/", new HttpHandle());
            server.start();
            logger.info("监听HTTP服务 0.0.0.0:" + ConfigManage.Configs.APIPort + " 成功");
            SuccessOne = true;
        } catch (Exception e) {
            logger.error("监听HTTP服务 0.0.0.0:" + ConfigManage.Configs.APIPort + " 失败",e);
        }


        try {
            KeyStore keyStore = KeyStore.getInstance("JKS");
            FileInputStream keyStoreIS = new FileInputStream(ConfigManage.Configs.APISSL_keyStorePath);
            keyStore.load(keyStoreIS, ConfigManage.Configs.APISSL_keyStorePassword.toCharArray());
            keyStoreIS.close();
            // 初始化密钥管理器
            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            keyManagerFactory.init(keyStore, ConfigManage.Configs.APISSL_keyStorePassword.toCharArray());
            // 初始化信任管理器
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(keyStore);


            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), new SecureRandom());

            server_https = HttpsServer.create(new InetSocketAddress(ConfigManage.Configs.APISSLPort), 0);
            server_https.createContext("/", new HttpHandle());

            server_https.setHttpsConfigurator(new HttpsConfigurator(sslContext) {
                public void configure(HttpsParameters params) {
                    SSLParameters defaultSSLParameters = sslContext.getDefaultSSLParameters();
                    params.setSSLParameters(defaultSSLParameters);
                }
            });


            server_https.start();
            logger.info("监听HTTPS服务 0.0.0.0:" + ConfigManage.Configs.APISSLPort + " 成功");
            SuccessOne = true;
        } catch (Exception e) {
            logger.error("监听HTTPS服务 0.0.0.0:" + ConfigManage.Configs.APISSLPort + " 失败",e);
        }

        if (!SuccessOne) {
            throw new Exception("HTTP和HTTPS服务均启动失败");
        }

        AddAPI("/",new index());
        AddAPI("/api/CreateConversation", new CreateConversation());
        AddAPI("/api/DeleteConversation", new DeleteConversation());
        AddAPI("/api/ConversationIsFound", new ConversationIsFound());
        AddAPI("/api/Chat", new Chat());
        AddAPI("/api/ChatStream", new ChatStream());
        AddAPI("/api/RenameConversation", new RenameConversation());
        AddAPI("/v1/chat/Completions", new Completions());
        AddAPI("/Ping", new Ping());
        AddAPI("/robots.txt", new robots());


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
        logger.info(t.getRequestMethod() + " " + t.getRequestURI().getPath());
        if ("OPTIONS".equals(t.getRequestMethod())) {
            t.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            t.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
            t.getResponseHeaders().add("Access-Control-Allow-Headers", "*");
            // 发送204 No Content响应表明请求成功，但没有消息实体
            t.sendResponseHeaders(204, -1);
            return;
        }
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
                    t.getRequestURI().getRawQuery() :
                    Stream2String(t.getRequestBody());
                        HandleType handle = new HandleType();
            handle.RequestParams = new JSONObject();
            if (query != null) {
                try {
                    handle.RequestParams = handle.RequestParams.parseObject(query);
                } catch (Exception ignored) {
                    for (String param : query.split("&")) {
                        String[] keyValue = param.split("=");
                        if (keyValue.length > 1) {
                            // 对key和value进行分别解码
                            String key = URLDecoder.decode(keyValue[0], StandardCharsets.UTF_8);
                            String value = URLDecoder.decode(keyValue[1], StandardCharsets.UTF_8);
                            handle.RequestParams.put(key, value);
                        } else if (keyValue.length == 1) {
                            // 只有key没有value时
                            String key = URLDecoder.decode(keyValue[0], StandardCharsets.UTF_8);
                            handle.RequestParams.put(key, "");
                        }
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
                handle.HttpExchange = t; // 传递过去
                Response = handler.handle(handle);
            }
            if (handle.HttpExchange_Disable_Default_Action) { return; } //关闭默认处理 用于支持特殊返回
        }
        t.getResponseHeaders().set("Content-Type", Response.Header_Content_Type);
        t.sendResponseHeaders(Response.code, Response.msg.getBytes(StandardCharsets.UTF_8).length);
        OutputStream os = t.getResponseBody();
        os.write(Response.msg.getBytes());
        os.close();
        t.close();
    }
    private String Stream2String(InputStream stream) {
        StringBuilder sb = new StringBuilder();
        try (InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
            int c;
            while ((c = reader.read()) != -1) {
                sb.append((char) c);
            }
        } catch (IOException e) {
            // TODO: handle exception
        }
        return sb.toString();
    }
}