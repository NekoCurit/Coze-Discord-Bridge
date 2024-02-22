package catx.feitu.coze_discord_bridge;

import catx.feitu.CozeProxy.CozeGPTConfig;
import catx.feitu.CozeProxy.Protocol.Protocols;
import catx.feitu.coze_discord_bridge.Config.ConfigBotsData;
import catx.feitu.coze_discord_bridge.Config.ConfigManage;
import catx.feitu.coze_discord_bridge.HttpServer.HttpServerManage;
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
            // Docker Helper (通过运行环境读取bot配置)
            if (!ConfigManage.configs.Disable_ENV_Read) {
                DockerHelper.dockerHelper();
            }
            // 无bot信息结束程序
            if (ConfigManage.configs.Bots.isEmpty()) {
                throw new Exception("未配置Bot信息 请编辑后重新启动");
            }
            // 初始化CozeGPT API
            Proxy proxy = ConfigManage.configs.UsingProxy ? new Proxy(
                    Objects.equals(ConfigManage.configs.ProxyType, "HTTP") ?
                            Proxy.Type.HTTP : Proxy.Type.SOCKS,
                    new InetSocketAddress(ConfigManage.configs.ProxyIP, ConfigManage.configs.ProxyPort)):
                    null;
            if (proxy != null) {
                logger.info("使用代理 " + ConfigManage.configs.ProxyType + "://" +
                        ConfigManage.configs.ProxyIP + ":" + ConfigManage.configs.ProxyPort + "/");
            }
            boolean successOne = false;
            for (int i = 0; ConfigManage.configs.Bots.size() > i; i++) {
                ConfigBotsData BotData = ConfigManage.configs.Bots.get(i);
                CozeGPTConfig GPTConfig = new CozeGPTConfig();
                try {
                    BotData.Key = Objects.equals(BotData.Key, "") ? "default" : BotData.Key;
                    if (Objects.equals(BotData.Token, "")) {
                        throw new Exception("无效的Token");
                    }
                    GPTConfig.loginApp = BotData.Protocol;
                    GPTConfig.token = BotData.Token;
                    GPTConfig.token2 = BotData.Token2;
                    GPTConfig.serverID = BotData.Server_id;
                    GPTConfig.Discord_CreateChannel_Category =  BotData.CreateChannel_Category;
                    GPTConfig.botID = BotData.CozeBot_id;

                    GPTConfig.generate_timeout = ConfigManage.configs.generate_timeout;

                    GPTConfig.Disable_2000Limit_Unlock = ConfigManage.configs.Disable_2000Limit_Unlock;
                    GPTConfig.Disable_Name_Cache = ConfigManage.configs.Disable_Name_Cache;
                    GPTConfig.Disable_CozeBot_ReplyMsgCheck = ConfigManage.configs.Disable_CozeBot_ReplyMsgCheck;

                    GPTConfig.Proxy = proxy;

                    if (GPTManage.containsGPT(BotData.Key)) {
                        logger.warn("[" + BotData.Key + "] 检测到重复的Bot 跳过加载");
                        continue;
                    }

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
        if (ConfigManage.configs.Keepalive_timer > 0) {
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