package catx.feitu.CozeProxy.Protocol.Listener;

import catx.feitu.CozeProxy.Protocol.UniversalEventListener;
import catx.feitu.CozeProxy.Protocol.UniversalEventListenerConfig;
import catx.feitu.CozeProxy.Protocol.UniversalMessage;
import org.javacord.api.entity.message.embed.Embed;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.event.message.MessageEditEvent;
import org.javacord.api.event.user.UserStartTypingEvent;
import org.javacord.api.listener.message.MessageCreateListener;
import org.javacord.api.listener.message.MessageEditListener;
import org.javacord.api.listener.user.UserStartTypingListener;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

public class DiscordListener implements MessageCreateListener, MessageEditListener, UserStartTypingListener{
    public UniversalEventListener handle;
    public UniversalEventListenerConfig config;
    public DiscordListener(UniversalEventListener handle, UniversalEventListenerConfig config) {
        this.handle = handle;
        this.config = config;
    }
    @Override
    public void onUserStartTyping(UserStartTypingEvent event) {
        if (handle == null) { return; }
        if (!Objects.equals(event.getChannel().asServerChannel().isPresent() ? event.getChannel().asServerChannel().get().getIdAsString() : null , config.filterServerID)) { return; }
        if (!Objects.equals(event.getUserIdAsString(), config.filterUserID)) { return; }
        handle.onStartGenerate(event.getChannel().getIdAsString());
    }
    @Override
    public void onMessageCreate(MessageCreateEvent event) {
        if (handle == null) { return; }
        if (!Objects.equals(event.getServer().isPresent() ? event.getServer().get().getIdAsString() : null , config.filterServerID)) { return; }

        if (Objects.equals(event.getMessageAuthor().getIdAsString(), config.filterSelfUserID)) {
            handle.onSelfMessageSend(event.getChannel().getIdAsString());
            return;
        }

        if (Objects.equals(event.getMessageAuthor().getIdAsString(), config.filterUserID)) {
            List<Embed> embeds = event.getMessage().getEmbeds(); // 获取消息中的所有嵌入内容
            List<String> files = new CopyOnWriteArrayList<>(); // 存储嵌入附件URL
            for (Embed embed : embeds) {
                if (embed.getImage().isPresent()) {
                    files.add(embed.getImage().get().getUrl().toString());
                }
            }
            handle.onMessageStream(event.getChannel().getIdAsString(),
                    new UniversalMessage()
                            .setContent(event.getMessageContent())
                            .setFiles(files)
                            .setHasButton(!event.getMessage().getComponents().isEmpty())
            );
        }
    }

    @Override
    public void onMessageEdit(MessageEditEvent event) {
        if (handle == null) { return; }
        if (!Objects.equals(event.getServer().isPresent() ? event.getServer().get().getIdAsString() : null , config.filterServerID)) { return; }
        if (Objects.equals(event.getMessageAuthor().getIdAsString(), config.filterUserID)) {
            List<Embed> embeds = event.getMessage().getEmbeds(); // 获取消息中的所有嵌入内容
            List<String> files = new CopyOnWriteArrayList<>(); // 存储嵌入附件URL
            for (Embed embed : embeds) {
                if (embed.getImage().isPresent()) {
                    files.add(embed.getImage().get().getUrl().toString());
                }
            }
            handle.onMessageStream(event.getChannel().getIdAsString(),
                    new UniversalMessage()
                            .setContent(event.getMessageContent())
                            .setFiles(files)
                            .setHasButton(!event.getMessage().getComponents().isEmpty())
            );
        }
    }
}