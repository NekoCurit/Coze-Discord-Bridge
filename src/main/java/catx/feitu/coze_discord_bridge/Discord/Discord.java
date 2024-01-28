package catx.feitu.coze_discord_bridge.Discord;

import catx.feitu.coze_discord_bridge.Config.ConfigManage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.entity.intent.Intent;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.Objects;

public class Discord {
    private static final Logger logger = LogManager.getLogger(Discord.class);

    public static DiscordApi api;

    public static void discord_init() {
        DiscordApiBuilder api_temp = new DiscordApiBuilder()
                .setToken(ConfigManage.Configs.Discord_Bot_Token)
                .addIntents(Intent.MESSAGE_CONTENT);
        if (ConfigManage.Configs.UsingProxy) {
            logger.info("使用代理:" + ConfigManage.Configs.ProxyIP + ":" + ConfigManage.Configs.ProxyPort);
            Proxy proxy = new Proxy(
                    Objects.equals(ConfigManage.Configs.ProxyType, "HTTP") ? Proxy.Type.HTTP : Proxy.Type.SOCKS,
                    new InetSocketAddress(ConfigManage.Configs.ProxyIP, ConfigManage.Configs.ProxyPort));
            api_temp = api_temp.setProxy(proxy);
        }
        api = api_temp.login().join();
        api.addListener(new MessageHandle());
    }
}