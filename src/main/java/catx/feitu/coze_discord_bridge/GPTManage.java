package catx.feitu.coze_discord_bridge;

import catx.feitu.coze_discord_bridge.Config.ConfigManage;
import catx.feitu.coze_discord_bridge.api.ConversationManage.ConversationHelper;
import catx.feitu.coze_discord_bridge.api.CozeGPT;
import catx.feitu.coze_discord_bridge.api.CozeGPTConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

public class GPTManage {
    private static final Logger logger = LogManager.getLogger(GPTManage.class);

    private static ConcurrentHashMap<Object, CozeGPT> ResponseMap = new ConcurrentHashMap<>();
    public static void newGPT(String botID, CozeGPTConfig config) throws Exception {
        CozeGPT GPT = new CozeGPT(config, true);
        GPT.setMark(botID);
        if (!config.Disable_2000Limit_Unlock) {
            if (new File("conversation_" + botID + ".json").exists()) {
                GPT.conversations = ConversationHelper.jsonString2Conversation(
                        Files.readString(Paths.get("conversation_" + botID + ".json"))
                );
            }
        }
        ResponseMap.put(botID, GPT);
    }
    public static CozeGPT getGPT(String botID) throws NullPointerException {
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
            } catch (Exception ignored) { }
            Files.writeString(new File("conversation_" + botID + ".json").toPath(), ConversationHelper.conversation2JsonString(GPT.conversations));
            ResponseMap.remove(botID);
        } catch (Exception ignored) {}
    }
    public static void clearGPT() {
        for (CozeGPT cozeGPT : ResponseMap.values()) {
            deleteGPT(cozeGPT.getMark());
        }
        ResponseMap.clear();
    }
    public static boolean containsGPT (String BotID) {
        return ResponseMap.containsKey(BotID);
    }
    public static void keepalive() {
        logger.info("[keepalive] 开始执行任务");
        int success = 0;
        int failed = 0;
        int noRequire = 0;
        for (CozeGPT cozeGPT : ResponseMap.values()) {
            if (Duration.between(
                    cozeGPT.getLatestReceiveCozeMsgInstant(),
                    Instant.now()).toMinutes() >
                    ConfigManage.configs.Keepalive_maxIntervalMinutes
            ) {
                try {
                    try { cozeGPT.GetConversationInfo(ConfigManage.configs.Keepalive_sendChannel); }
                    catch (Exception e) { cozeGPT.CreateConversation(ConfigManage.configs.Keepalive_sendChannel); }
                    cozeGPT.Chat(ConfigManage.configs.Keepalive_sendChannel ,ConfigManage.configs.Keepalive_sendMessage);
                    logger.info("[keepalive] " + cozeGPT.getMark() + " 成功");
                    success++;
                } catch (Exception e) {
                    logger.warn("[keepalive] " + cozeGPT.getMark() + " 失败", e);
                    failed++;
                }
            } else {
                noRequire++;
            }
        }
        logger.info("[keepalive] 执行完毕  成功:" + success + " 失败:" + failed + " 无需执行:" + noRequire);
    }
}
