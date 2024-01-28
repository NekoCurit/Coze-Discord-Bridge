package catx.feitu.coze_discord_bridge.Discord;

import catx.feitu.coze_discord_bridge.Misc.CacheManager;
import catx.feitu.coze_discord_bridge.Config.ConfigManage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.javacord.api.entity.message.MessageAttachment;
import org.javacord.api.entity.message.embed.Embed;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.event.message.MessageEditEvent;
import org.javacord.api.listener.message.MessageCreateListener;
import org.javacord.api.listener.message.MessageEditListener;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MessageHandle implements MessageCreateListener, MessageEditListener {
    private static final Logger logger = LogManager.getLogger(MessageHandle.class);

    @Override
    public void onMessageCreate(MessageCreateEvent event) {
        if (Objects.equals(event.getMessageAuthor().getIdAsString(), event.getApi().getYourself().getIdAsString())) {
            logger.info("[Send] " + event.getChannel().getIdAsString() + ": " + event.getMessageContent());
        }
    }

    @Override
    public void onMessageEdit(MessageEditEvent event) {
        if (Objects.equals(event.getMessageAuthor().getIdAsString(), ConfigManage.Configs.CozeBot_id)) {
            if (ConfigManage.Configs.Ignore_CozeBot_ReplyMsgCheck || event.getMessage().getMentionedUsers().contains(event.getApi().getYourself())) {
                Boolean Done100 = !event.getMessage().getComponents().isEmpty(); //存在按钮 = 100%响应完毕
                if (Done100) {
                    logger.info("[CozeBot] " + event.getChannel().getIdAsString() + ":" + event.getMessageContent());
                }
                List<Embed> embeds = event.getMessage().getEmbeds(); // 获取消息中的所有嵌入内容
                List<String> files = new ArrayList<>(); // 存储嵌入图片的URL
                for (Embed embed : embeds) {
                    if (embed.getImage().isPresent()) {
                        if (Done100) {
                            logger.info("[CozeBot] 图片URL -> " + embed.getImage().get().getUrl().toString());
                        }
                        files.add(embed.getImage().get().getUrl().toString());
                    }
                }
                CacheManager.Cache_BotReplySave( // 不是 Done100 也要记录  因为少数情况生成完成Bot也不会显示按钮(你可以问bot nsfw试试)
                        event.getChannel().getIdAsString(),
                        event.getMessageContent(),
                        files,
                        Done100
                );
            }
        }
    }
}