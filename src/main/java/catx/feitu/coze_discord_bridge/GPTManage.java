package catx.feitu.coze_discord_bridge;

import catx.feitu.coze_discord_bridge.api.CozeGPT;
import catx.feitu.coze_discord_bridge.api.CozeGPTConfig;

import java.util.concurrent.ConcurrentHashMap;

public class GPTManage {
    private final ConcurrentHashMap<Object, CozeGPT> ResponseMap = new ConcurrentHashMap<>();
    public void newGPT(String botID, CozeGPTConfig config) throws Exception {
        ResponseMap.put(botID, new CozeGPT(config, true));
    }
    public CozeGPT getGPT(String botID) {
        if (!ResponseMap.containsKey(botID)) {
            throw new NullPointerException();
        }
        return ResponseMap.get(botID);
    }
    public void deleteGPT(String botID) {
        ResponseMap.remove(botID);
    }

}
