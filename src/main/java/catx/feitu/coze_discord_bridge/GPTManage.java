package catx.feitu.coze_discord_bridge;

import catx.feitu.coze_discord_bridge.Config.YmalReslove.YamlConfig;
import catx.feitu.coze_discord_bridge.api.ConversationManage.ConversationHelper;
import catx.feitu.coze_discord_bridge.api.CozeGPT;
import catx.feitu.coze_discord_bridge.api.CozeGPTConfig;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.ConcurrentHashMap;

public class GPTManage {
    private static ConcurrentHashMap<Object, CozeGPT> ResponseMap = new ConcurrentHashMap<>();
    public static void newGPT(String botID, CozeGPTConfig config) throws Exception {
        CozeGPT GPT = new CozeGPT(config, true);
        GPT.setMark(botID);
        if (!config.Disable_2000Limit_Unlock) {
            if (new File("conversation_" + botID + ".json").exists()) {
                GPT.conversations = ConversationHelper.JsonString2Conversation(
                        Files.readString(Paths.get("conversation_" + botID + ".json"))
                );
            }
        }
        ResponseMap.put(botID, GPT);
    }
    public static CozeGPT getGPT(String botID) throws Exception {
        if (!ResponseMap.containsKey(botID)) {
            throw new NullPointerException();
        }
        return ResponseMap.get(botID);
    }
    public static void deleteGPT(String botID) {
        try {
            CozeGPT GPT = ResponseMap.get(botID);
            try {
                GPT.Logout();
            } catch (Exception ignored) {
            }
            Files.writeString(new File("conversation_" + botID + ".json").toPath(), ConversationHelper.Conversation2JsonString(GPT.conversations));
            ResponseMap.remove(botID);
        } catch (Exception ignored) {}
    }
    public static void clearGPT() {
        for (CozeGPT cozeGPT : ResponseMap.values()) {
            deleteGPT(cozeGPT.getMark());
        }
        ResponseMap.clear();
    }
}
