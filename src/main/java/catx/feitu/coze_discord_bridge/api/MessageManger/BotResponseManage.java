package catx.feitu.coze_discord_bridge.api.MessageManger;

import java.util.concurrent.ConcurrentHashMap;

public class BotResponseManage {
    private final ConcurrentHashMap<Object, BotResponseType> ResponseMap = new ConcurrentHashMap<>();
    public void saveMsg(String ChannelID, BotResponseType Response) {
        ResponseMap.put(ChannelID, Response);
    }
    public BotResponseType getMsg(String ChannelID) {
        if (!ResponseMap.containsKey(ChannelID)) {
            throw new NullPointerException();
        }
        return ResponseMap.get(ChannelID);
    }
}
