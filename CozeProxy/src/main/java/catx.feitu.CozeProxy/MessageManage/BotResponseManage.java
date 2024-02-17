package catx.feitu.CozeProxy.MessageManage;

import java.util.concurrent.ConcurrentHashMap;

public class BotResponseManage {
    private final ConcurrentHashMap<Object, BotResponseType> ResponseMap = new ConcurrentHashMap<>();
    public void saveMsg(String channelID, BotResponseType response) {
        ResponseMap.put(channelID, response);
    }
    public BotResponseType getMsg(String channelID) throws NullPointerException {
        if (!ResponseMap.containsKey(channelID)) {
            throw new NullPointerException();
        }
        return ResponseMap.get(channelID);
    }
    public void clearMsg(String channelID) {
        ResponseMap.remove(channelID);
    }

}
