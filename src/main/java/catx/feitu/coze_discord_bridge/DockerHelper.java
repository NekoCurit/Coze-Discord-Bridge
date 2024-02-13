package catx.feitu.coze_discord_bridge;

import catx.feitu.coze_discord_bridge.Config.ConfigBotsData;
import catx.feitu.coze_discord_bridge.Config.ConfigManage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class DockerHelper {
    private static final Logger logger = LogManager.getLogger(DockerHelper.class);

    public static void dockerHelper() {
        if (System.getenv("token") == null) { return; }

        ConfigBotsData config = new ConfigBotsData();

        config.Discord_Bot_Token = System.getenv("token");
        config.Server_id = System.getenv("discord_server");
        config.CozeBot_id = System.getenv("coze_bot");
        config.Key = System.getenv("key") != null ?
                System.getenv("key") :
                "default";

        ConfigManage.configs.Bots.add(config);
        logger.info("读取Docker ENV bot信息\n" +
                "Token: ************\n" +
                "Discord Server ID: " + config.Server_id + "\n" +
                "CozeBot ID: " + config.CozeBot_id + "\n" +
                "Key: " + (config.Key.equals("default") ? "default" : "************") + "\n" +
                "请确保上述信息没有 null");
    }
}