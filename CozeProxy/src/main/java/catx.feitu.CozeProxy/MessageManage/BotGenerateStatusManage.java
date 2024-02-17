package catx.feitu.CozeProxy.MessageManage;

import java.util.concurrent.ConcurrentHashMap;

public class BotGenerateStatusManage {
    private final ConcurrentHashMap<String, Boolean> startGenerates = new ConcurrentHashMap<>();

    public void saveGenerateStatus(String channelID) {
        startGenerates.put(channelID, true);
    }

    public void clearGenerateStatus(String channelID) {
        startGenerates.remove(channelID);
    }

    public boolean getGenerateStatus(String channelID) {
        return startGenerates.getOrDefault(channelID, false);
    }
}