package catx.feitu.coze_discord_bridge;

import catx.feitu.coze_discord_bridge.Config.ConfigBotsData;
import catx.feitu.coze_discord_bridge.Config.ConfigManage;
import catx.feitu.coze_discord_bridge.HttpServer.HttpServerManage;
import catx.feitu.coze_discord_bridge.api.CozeGPTConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.fusesource.jansi.AnsiConsole;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.Objects;


public class Main {
    private static final Logger logger = LogManager.getLogger(Main.class);

    public static void main(String[] args) {
        AnsiConsole.systemInstall();
        try {
            ConfigManage.DefaultConfig();

            ConfigManage.ReadConfig();
            // 初始化CozeGPT API
            if (ConfigManage.Configs.Bots.isEmpty()) {
                throw new Exception("未配置Bot信息 请编辑后重新启动");
            }
            Proxy proxy = ConfigManage.Configs.UsingProxy ? new Proxy(
                    Objects.equals(ConfigManage.Configs.ProxyType, "HTTP") ?
                            Proxy.Type.HTTP : Proxy.Type.SOCKS,
                    new InetSocketAddress(ConfigManage.Configs.ProxyIP, ConfigManage.Configs.ProxyPort)):
                    null;
            if (proxy != null) {
                logger.info("使用代理 " + ConfigManage.Configs.ProxyType + "://" +
                        ConfigManage.Configs.ProxyIP + ":" + ConfigManage.Configs.ProxyPort + "/");
            }
            boolean successOne = false;
            for (int i = 0;ConfigManage.Configs.Bots.size() > i;i++) {
                ConfigBotsData BotData = ConfigManage.Configs.Bots.get(i);
                CozeGPTConfig GPTConfig = new CozeGPTConfig();
                try {
                    BotData.Key = Objects.equals(BotData.Key, "") ? "default" : BotData.Key;
                    if (Objects.equals(BotData.Discord_Bot_Token, "")) {
                        throw new Exception("无效的Discord_Bot_Token");
                    }
                    GPTConfig.Discord_Bot_Token = BotData.Discord_Bot_Token;
                    GPTConfig.Server_id = BotData.Server_id;
                    GPTConfig.Discord_CreateChannel_Category =  BotData.CreateChannel_Category;
                    GPTConfig.CozeBot_id = BotData.CozeBot_id;

                    GPTConfig.generate_timeout = ConfigManage.Configs.generate_timeout;

                    GPTConfig.Disable_2000Limit_Unlock = ConfigManage.Configs.Disable_2000Limit_Unlock;
                    GPTConfig.Disable_Name_Cache = ConfigManage.Configs.Disable_Name_Cache;
                    GPTConfig.Disable_CozeBot_ReplyMsgCheck = ConfigManage.Configs.Disable_CozeBot_ReplyMsgCheck;

                    GPTConfig.Proxy = proxy;


                    logger.info("[" + BotData.Key + "] 开始登录流程..");
                    GPTManage.newGPT(BotData.Key, GPTConfig);
                    logger.info("[" + BotData.Key + "] 初始化成功");
                    successOne = true;
                } catch (Exception e){
                    logger.error("[" + BotData.Key + "] 初始化失败", e);
                }
            }
            if (!successOne) {
                throw new Exception("配置中的所有Bot配置均加载失败");
            }
            HttpServerManage.start();
        } catch (Exception e) {
            logger.error(e);
            System.exit(-1);
        }
        KeepaliveTimer Keepalive = new KeepaliveTimer();
        if (ConfigManage.Configs.Keepalive_timer > 0) {
            Keepalive.start();
        }
        // 程序退出前执行
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            Keepalive.stop();
            GPTManage.clearGPT();
            HttpServerManage.stop();

            AnsiConsole.systemUninstall();
        }));
    }
}