package catx.feitu.coze_discord_bridge.HttpServer.api;

import catx.feitu.coze_discord_bridge.HttpServer.APIHandler;
import catx.feitu.coze_discord_bridge.HttpServer.HandleType;
import catx.feitu.coze_discord_bridge.HttpServer.ResponseType;
import com.alibaba.fastjson.JSONObject;

import java.time.Instant;

public class robots implements APIHandler {

    @Override
    public ResponseType handle(HandleType Handle) {
        ResponseType Response = new ResponseType();

        Response.msg = "User-agent: *\nDisallow: /\n";
        Response.code = 200;
        Response.Header_Content_Type = "text/plain; charset=UTF-8";

        return Response;
    }
}
