package catx.feitu.coze_discord_bridge;

import catx.feitu.coze_discord_bridge.Config.ConfigManage;
import catx.feitu.coze_discord_bridge.Discord.Discord;
import catx.feitu.coze_discord_bridge.HttpServer.HttpServerManage;
import catx.feitu.coze_discord_bridge.Misc.CacheManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.fusesource.jansi.AnsiConsole;

import java.io.IOException;
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
            HttpServerManage.Start(ConfigManage.Configs.APIPort);
        } catch (IOException e) {
            logger.error("监听 0.0.0.0:" + ConfigManage.Configs.APIPort + " 失败,请关闭占用程序或者修改端口后重新启动",e);
            System.exit(-1);
        }
        logger.info("监听 0.0.0.0:" + ConfigManage.Configs.APIPort + " 成功");

        logger.info("Coze-Discord-Bridge 初始化完毕,正在登录Discord...");
        Discord.discord_init();
    }
}
