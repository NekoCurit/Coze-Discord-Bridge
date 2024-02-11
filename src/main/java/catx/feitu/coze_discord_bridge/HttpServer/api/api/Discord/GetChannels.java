package catx.feitu.coze_discord_bridge.HttpServer.api.api.Discord;

import catx.feitu.coze_discord_bridge.HttpServer.APIHandler;
import catx.feitu.coze_discord_bridge.HttpServer.HandleType;
import catx.feitu.coze_discord_bridge.HttpServer.ResponseType;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.javacord.api.entity.channel.ChannelCategory;
import org.javacord.api.entity.channel.RegularServerChannel;

import java.util.ArrayList;
import java.util.List;

public class GetChannels implements APIHandler {
    @Override
    public ResponseType handle(HandleType Handle) {
        ResponseType Response = new ResponseType();
        JSONObject json = new JSONObject(true);
        try {
            List<Long> categoryChannels = new ArrayList<>();

            JSONArray json_data = new JSONArray();
            Handle.CozeGPT.private_getServer();
            List<ChannelCategory> categorys = Handle.CozeGPT.server.getChannelCategories();
            // 处理有分组的频道
            for (ChannelCategory category : categorys) {
                JSONObject json_data_categorys = new JSONObject(true);
                JSONArray json_data_categorys_channels = new JSONArray();

                json_data_categorys.put("name",category.getName());
                json_data_categorys.put("id",category.getId());

                categoryChannels.add(category.getId());

                List<RegularServerChannel> channels = category.getChannels();
                for (RegularServerChannel channel : channels) {
                    JSONObject json_data_categorys_channels_channel = new JSONObject(true);

                    categoryChannels.add(channel.getId());

                    json_data_categorys_channels_channel.put("id",channel.getId());
                    json_data_categorys_channels_channel.put("name",channel.getName());

                    json_data_categorys_channels.add(json_data_categorys_channels_channel);
                }
                json_data_categorys.put("channels",json_data_categorys_channels);
                json_data.add(json_data_categorys);
            }
            // 处理没有分组的频道
            JSONArray json_data_no_group_channels = new JSONArray();
            List<RegularServerChannel> noGroupChannels = Handle.CozeGPT.server.getRegularChannels();
            for (RegularServerChannel channel : noGroupChannels) {
                if (categoryChannels.contains(channel.getId())) { continue; }
                JSONObject json_data_no_group_channel = new JSONObject(true);
                json_data_no_group_channel.put("id", channel.getId());
                json_data_no_group_channel.put("name", channel.getName());

                json_data_no_group_channels.add(json_data_no_group_channel);
            }
            JSONObject noGroupChannelsData = new JSONObject(true);
            noGroupChannelsData.put("name", "default");
            noGroupChannelsData.put("id", null);
            noGroupChannelsData.put("channels", json_data_no_group_channels);
            json_data.add(noGroupChannelsData);


            Response.code = 200;
            json.put("code", 200);
            json.put("message", "获取信息成功");
            json.put("data", json_data);

        } catch (Exception e) {
            Response.code = 502;
            json.put("code", 502);
            json.put("message", "获取信息失败");
            JSONObject json_data = new JSONObject(true);
            json.put("data", json_data);
        }
        Response.msg = json.toJSONString();
        return Response;
    }
}
