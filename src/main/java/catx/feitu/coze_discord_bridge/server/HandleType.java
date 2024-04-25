package catx.feitu.coze_discord_bridge.server;

import catx.feitu.CozeProxy.CozeGPT;
import com.alibaba.fastjson.JSONObject;
import com.sun.net.httpserver.HttpExchange;

public class HandleType {
    public String RequestPath = "";
    public JSONObject RequestParams;
    public HttpExchange HttpExchange;
    public boolean HttpExchange_Disable_Default_Action = false;
    public CozeGPT CozeGPT;
}
