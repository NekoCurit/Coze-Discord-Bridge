package catx.feitu.CozeProxy.protocol.listene.api;

import catx.feitu.CozeProxy.protocol.listene.EventListen;
import catx.feitu.CozeProxy.protocol.listene.EventListenConfig;
import catx.feitu.CozeProxy.protocol.message.MessageBuilder;
import catx.feitu.DiscordSelfClient.client.SelfClient;
import catx.feitu.DiscordSelfClient.client.impl.Message;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;


public class DiscordListener {
    public void listen(SelfClient api_discord , String channelID , EventListen eventListen, EventListenConfig config) {
        Thread thread = new Thread(() -> { // Websocket监听器不会写(其实是太耗时了)
            try {
                Thread.sleep(1000);
                int attempt = 0; // 重试次数
                Message latestMessage = api_discord.getLatestMessage(channelID);
                if (!latestMessage.getUser().isBot() && (config.filterReply || latestMessage.getMentions().contains(config.filterSelfUserID))) { // 如果是bot就已经出现 不需要再等待
                    while (!latestMessage.getUser().isBot()) {
                        if (attempt > 20) { return; }
                        latestMessage = api_discord.getLatestMessage(channelID);
                        try { Thread.sleep(500); } catch (InterruptedException ignored) {}
                        attempt++;
                    }
                }
                eventListen.onStartGenerate(channelID);
                attempt = 0;
                while (attempt < 120) {
                    latestMessage = api_discord.getMessage(channelID ,latestMessage.getId());
                    List<String> eventFiles = new CopyOnWriteArrayList<>(); // 存储嵌入附件URL
                    for (catx.feitu.DiscordSelfClient.client.impl.Attachment attachment : latestMessage.getAttachments()) {
                        eventFiles.add(attachment.getUrl());
                    }
                    eventListen.onMessageStream(channelID ,new MessageBuilder()
                            .setContent(latestMessage.getContent())
                            .setFiles(eventFiles)
                            .setHasButton(latestMessage.isHasComponents())
                    );
                    if (latestMessage.isHasComponents()) { return; }
                    try { Thread.sleep(1000); } catch (InterruptedException ignored) {}
                    attempt++;
                }
            } catch (Exception ignored) { }
        });
        thread.start();
    }
}