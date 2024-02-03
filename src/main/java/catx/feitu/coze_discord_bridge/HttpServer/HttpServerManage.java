package catx.feitu.coze_discord_bridge.HttpServer;

import catx.feitu.coze_discord_bridge.Config.ConfigManage;
import catx.feitu.coze_discord_bridge.GPTManage;
import catx.feitu.coze_discord_bridge.HttpServer.api.Ping;
import catx.feitu.coze_discord_bridge.HttpServer.api.api.*;
import catx.feitu.coze_discord_bridge.HttpServer.api.index;
import catx.feitu.coze_discord_bridge.HttpServer.api.robots;
import catx.feitu.coze_discord_bridge.HttpServer.api.v1.Models;
import catx.feitu.coze_discord_bridge.HttpServer.api.v1.chat.Completions;
import catx.feitu.coze_discord_bridge.HttpServer.api.v1.images.Generations;
import com.alibaba.fastjson.JSON;
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
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import static catx.feitu.coze_discord_bridge.HttpServer.HttpServerManage.APIS;

public class HttpServerManage {

    public static HttpServer server;
    public static HttpsServer server_https;

    static Map<String, APIHandler> APIS = new HashMap<>();
    static List<String> ProtectPaths = new ArrayList<>();

    private static ThreadPoolExecutor threadPoolExecutor;

    private static final Logger logger = LogManager.getLogger(HttpServerManage.class);

    public static void start() throws Exception {
        boolean SuccessOne = false;
        try {
            threadPoolExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(ConfigManage.Configs.APIMaxThread);
            logger.info("初始化线程池成功 最大线程:" + ConfigManage.Configs.APIMaxThread);
        } catch (Exception e){
            logger.error("初始化线程池失败" ,e);
            throw e;
        }

        try {
            if (ConfigManage.Configs.APIPort != 0) {
                server = HttpServer.create(new InetSocketAddress(ConfigManage.Configs.APIPort), 0);
                server.createContext("/", new HttpHandle());
                server.setExecutor(threadPoolExecutor); // 线程池设置
                server.start();
                logger.info("监听HTTP服务 0.0.0.0:" + ConfigManage.Configs.APIPort + " 成功");
                SuccessOne = true;
            }
        } catch (Exception e) {
            logger.error("监听HTTP服务 0.0.0.0:" + ConfigManage.Configs.APIPort + " 失败" ,e);
        }


        try {
            if (ConfigManage.Configs.APISSLPort != 0) {
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

                server_https.setExecutor(threadPoolExecutor); // 线程池设置

                server_https.setHttpsConfigurator(new HttpsConfigurator(sslContext) {
                    public void configure(HttpsParameters params) {
                        SSLParameters defaultSSLParameters = sslContext.getDefaultSSLParameters();
                        params.setSSLParameters(defaultSSLParameters);
                    }
                });


                server_https.start();
                logger.info("监听HTTPS服务 0.0.0.0:" + ConfigManage.Configs.APISSLPort + " 成功");
                SuccessOne = true;
            }
        } catch (Exception e) {
            logger.error("监听HTTPS服务 0.0.0.0:" + ConfigManage.Configs.APISSLPort + " 失败",e);
        }

        if (!SuccessOne) {
            throw new Exception("HTTP和HTTPS服务均启动失败");
        }

        AddAPI("/",new index(), false);
        AddAPI("/Ping", new Ping(), false);
        AddAPI("/robots.txt", new robots(), false);
        //AddAPI("/favicon.ico", , false);

        AddAPI("/api/CreateConversation", new CreateConversation(), true);
        AddAPI("/api/DeleteConversation", new DeleteConversation(), true);
        AddAPI("/api/ConversationIsFound", new ConversationIsFound(), true);
        AddAPI("/api/Chat", new Chat(), true);
        AddAPI("/api/ChatStream", new ChatStream(), true);
        AddAPI("/api/RenameConversation", new RenameConversation(), true);

        AddAPI("/v1/models", new Models(), true);
        AddAPI("/v1/chat/Completions", new Completions(), true);
        AddAPI("/v1/images/Generations", new Generations(), true);
    }
    public static void stop() {
        try { server.stop(0); } catch (Exception ignored) {}
        logger.info("停止HTTP服务成功");
        try { server_https.stop(0); } catch (Exception ignored) {}
        logger.info("停止HTTPS服务成功");
        try { threadPoolExecutor.shutdown(); } catch (Exception ignored) {}
        logger.info("关闭线程池成功");
    }
    public static void AddAPI (String s ,APIHandler api,boolean IsProtect) {
        APIS.put(s.toLowerCase(), api);
        if (IsProtect) {
            ProtectPaths.add(s.toLowerCase());
        }
    }
}
class HttpHandle implements HttpHandler {
    private static final Logger logger = LogManager.getLogger(HttpServerManage.class);
    @Override
    public void handle(HttpExchange t) throws IOException {
        try {
            ResponseType Response = new ResponseType();
            logger.info(t.getRemoteAddress().getHostString() + ":" + t.getRemoteAddress().getPort() + "  " +
                    t.getRequestMethod() + " " + t.getRequestURI().getPath());
            t.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
            if ("OPTIONS".equals(t.getRequestMethod())) {
                t.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, OPTIONS, POST");
                t.getResponseHeaders().add("Access-Control-Allow-Headers", "*");
                t.sendResponseHeaders(200, 0);
                OutputStream os = t.getResponseBody();
                os.write(0);
                os.close();
                t.close();
                return;
            }
            APIHandler handler = APIS.get(t.getRequestURI().getPath().toLowerCase());
            if (handler == null) { // 404 Not Found
                JSONObject json = new JSONObject(true);
                json.put("code", 404);
                json.put("message", "终结点不存在");
                Response.msg = json.toJSONString();
                Response.code = 404;
            } else { // 200 Successful
                String query = "GET".equals(t.getRequestMethod()) ?
                        t.getRequestURI().getRawQuery() :
                        Stream2String(t.getRequestBody());
                HandleType handle = new HandleType();
                handle.RequestParams = new JSONObject(true);
                if (query != null) {
                    try {
                        handle.RequestParams = JSON.parseObject(query);
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
                String Key = handle.RequestParams.containsKey("key") ? handle.RequestParams.getString("key") : "";
                Key = Objects.equals(Key, "") ? t.getRequestHeaders().getFirst("key") : Key;
                Key = Objects.equals(Key, "") ? t.getRequestHeaders().getFirst("Authorization").replace("Bearer ", "") : Key;

                Key = Objects.equals(Key, null) ? "default" : Key;

                try {
                    handle.CozeGPT = GPTManage.getGPT(Key);
                    if (handle.CozeGPT == null) {
                        throw new Exception();
                    }
                    handle.RequestPath = t.getRequestURI().getPath();
                    handle.HttpExchange = t; // 传递过去
                    Response = handler.handle(handle);

                    if (handle.HttpExchange_Disable_Default_Action) {
                        return;
                    } // 关闭默认处理 用于支持特殊返回
                } catch (Exception ignored) {
                    JSONObject json = new JSONObject(true);
                    json.put("code", 403);
                    json.put("message", "无权访问本服务");
                    Response.msg = json.toJSONString();
                    Response.code = 403;
                }

            }
            t.getResponseHeaders().set("Content-Type", Response.Header_Content_Type);

            byte[] ResponseByte = Response.msg.getBytes(StandardCharsets.UTF_8);
            t.sendResponseHeaders(Response.code, ResponseByte.length);
            OutputStream os = t.getResponseBody();
            os.write(ResponseByte);
            os.close();

            t.close();
        } catch (Exception e){
            logger.error("请求处理失败" ,e);
            throw e;
        }
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