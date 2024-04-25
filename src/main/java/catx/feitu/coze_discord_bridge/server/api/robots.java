package catx.feitu.coze_discord_bridge.server.api;

import catx.feitu.coze_discord_bridge.server.APIHandler;
import catx.feitu.coze_discord_bridge.server.HandleType;
import catx.feitu.coze_discord_bridge.server.ResponseType;

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
