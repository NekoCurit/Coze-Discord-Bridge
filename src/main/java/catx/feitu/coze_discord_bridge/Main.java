package catx.feitu.coze_discord_bridge;

import catx.feitu.coze_discord_bridge.Config.ConfigManage;
import catx.feitu.coze_discord_bridge.Discord.Discord;
import catx.feitu.coze_discord_bridge.HttpServer.HttpServerManage;
import catx.feitu.coze_discord_bridge.Misc.CacheManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.fusesource.jansi.AnsiConsole;

import java.util.Objects;


public class Main {
    private static final Logger logger = LogManager.getLogger(Main.class);

    public static void main(String[] args) {
        AnsiConsole.systemInstall();

        ConfigManage.DefaultConfig();
        ConfigManage.ReadConfig();

        if (Objects.equals(ConfigManage.Configs.Discord_Bot_Token, "")) {
            logger.error("Discord_Bot_Token为空,请编辑后重新启动");
            System.exit(-1);
        }
        if (!ConfigManage.Configs.Disable_Name_Cache) {
            CacheManager.LoadCache();
        }
        try {
            HttpServerManage.Start();
        } catch (Exception e) {
            logger.error(e);
            System.exit(-1);
        }

        logger.info("Coze-Discord-Bridge 初始化完毕,正在登录Discord...");
        // 程序退出前执行
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            Discord.api.disconnect();
        }));
        // Discord 登录
        Discord.discord_init();
    }
}