package catx.feitu.CozeProxy.Protocol;

import catx.feitu.CozeProxy.Protocol.Listener.DiscordListener;
import catx.feitu.CozeProxy.Protocol.Listener.SlackListener;
import com.slack.api.bolt.App;
import com.slack.api.bolt.AppConfig;
import com.slack.api.bolt.socket_mode.SocketModeApp;
import com.slack.api.model.event.MessageEvent;
import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.entity.intent.Intent;

import java.io.IOException;
import java.net.Proxy;

public class Protocol {
    public String apiSelected;
    public DiscordApi api_discord;
    public App api_slack;
    public UniversalEventListen eventListen;
    public Protocol (String protocol , String token) {

    }
    public void login (String protocol , String token , Proxy proxy) throws Exception {
        switch (protocol){
            case "discord":
                api_discord.addListener(new DiscordListener(eventListen));
                api_discord = new DiscordApiBuilder()
                        .setToken(token)
                        .addIntents(Intent.MESSAGE_CONTENT)
                        .setProxy(proxy)
                        .login()
                        .join();
            case "slack":
                AppConfig config = new AppConfig();
                config.setSingleTeamBotToken(token);
                api_slack = new App(config);
                api_slack.event(MessageEvent.class, new SlackListener(eventListen));
                new SocketModeApp(api_slack).start();

        }
    }
}
