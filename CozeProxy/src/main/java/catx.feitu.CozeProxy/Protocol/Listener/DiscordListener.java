package catx.feitu.CozeProxy.Protocol.Listener;

import catx.feitu.CozeProxy.Protocol.UniversalEventListen;
import catx.feitu.CozeProxy.Protocol.UniversalMessage;
import org.javacord.api.entity.message.embed.Embed;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.event.message.MessageEditEvent;
import org.javacord.api.event.user.UserStartTypingEvent;
import org.javacord.api.listener.message.MessageCreateListener;
import org.javacord.api.listener.message.MessageEditListener;
import org.javacord.api.listener.user.UserStartTypingListener;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class DiscordListener implements MessageCreateListener, MessageEditListener, UserStartTypingListener{
    public UniversalEventListen handle;
    public DiscordListener(UniversalEventListen handle) {
        this.handle = handle;
    }

    @Override
    public void onUserStartTyping(UserStartTypingEvent event) {
        handle.onUserStartTyping(null ,event.getChannel().getIdAsString(), event.getUserIdAsString());
    }
    @Override
    public void onMessageCreate(MessageCreateEvent event) {
        List<Embed> embeds = event.getMessage().getEmbeds(); // 获取消息中的所有嵌入内容
        List<String> files = new CopyOnWriteArrayList<>(); // 存储嵌入附件URL
        for (Embed embed : embeds) {
            if (embed.getImage().isPresent()) {
                files.add(embed.getImage().get().getUrl().toString());
            }
        }
        handle.onMessageCreate(null ,event.getChannel().getIdAsString(),
                event.getMessage().getAuthor().getIdAsString(),
                new UniversalMessage()
                        .setContent(event.getMessageContent())
                        .setFiles(files)
                        .setHasButton(!event.getMessage().getComponents().isEmpty())
                );
    }

    @Override
    public void onMessageEdit(MessageEditEvent event) {
        List<Embed> embeds = event.getMessage().getEmbeds(); // 获取消息中的所有嵌入内容
        List<String> files = new CopyOnWriteArrayList<>(); // 存储嵌入附件URL
        for (Embed embed : embeds) {
            if (embed.getImage().isPresent()) {
                files.add(embed.getImage().get().getUrl().toString());
            }
        }
        handle.onMessageEdit(null ,event.getChannel().getIdAsString(),
                event.getMessage().getAuthor().getIdAsString(),
                new UniversalMessage()
                        .setContent(event.getMessageContent())
                        .setFiles(files)
                        .setHasButton(!event.getMessage().getComponents().isEmpty())
        );
    }
}