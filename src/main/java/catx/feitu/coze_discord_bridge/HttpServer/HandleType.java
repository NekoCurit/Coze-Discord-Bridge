package catx.feitu.coze_discord_bridge.HttpServer;

import com.alibaba.fastjson.JSONObject;
import com.sun.net.httpserver.HttpExchange;

public class HandleType {

    public String RequestPath = "";

    public JSONObject RequestParams;

    public HttpExchange HttpExchange;

    public boolean HttpExchange_Disable_Default_Action = false;

}
