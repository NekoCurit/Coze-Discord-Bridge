package catx.feitu.coze_discord_bridge.api.Listen;

import catx.feitu.coze_discord_bridge.Config.ConfigManage;
import catx.feitu.coze_discord_bridge.Misc.CacheManager;
import catx.feitu.coze_discord_bridge.api.MessageManger.BotResponseManage;
import catx.feitu.coze_discord_bridge.api.MessageManger.BotResponseType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.javacord.api.entity.message.embed.Embed;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.event.message.MessageEditEvent;
import org.javacord.api.event.user.UserStartTypingEvent;
import org.javacord.api.listener.message.MessageCreateListener;
import org.javacord.api.listener.message.MessageEditListener;
import org.javacord.api.listener.user.UserStartTypingListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MessageListener implements MessageCreateListener, MessageEditListener, UserStartTypingListener {
    private static final Logger logger = LogManager.getLogger(MessageListener.class);
    private BotResponseManage BotResponseManage;
    public MessageListener(BotResponseManage BotResponseManage) {
        this.BotResponseManage = BotResponseManage;
    }

    @Override
    public void onUserStartTyping(UserStartTypingEvent event) {
        if(Objects.equals(event.getUserIdAsString(), ConfigManage.Configs.CozeBot_id)) {
            CacheManager.Cache_BotStartGenerate_Write(event.getChannel().getIdAsString());
            logger.info("[CozeBot Start Generate] " + event.getChannel().getIdAsString());
        }
    }
    @Override
    public void onMessageCreate(MessageCreateEvent event) {
        if (Objects.equals(event.getMessageAuthor().getIdAsString(), event.getApi().getYourself().getIdAsString())) {
            logger.info("[Send] " + event.getChannel().getIdAsString() + ": " + event.getMessageContent());
        }
    }

    @Override
    public void onMessageEdit(MessageEditEvent event) {
        if (Objects.equals(event.getMessageAuthor().getIdAsString(), ConfigManage.Configs.CozeBot_id)) {
            if (ConfigManage.Configs.Disable_CozeBot_ReplyMsgCheck || event.getMessage().getMentionedUsers().contains(event.getApi().getYourself())) {
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
                BotResponseType Response = new BotResponseType();
                Response.prompt = event.getMessageContent();
                Response.files = files;
                Response.SetCompleted(Done100);
                this.BotResponseManage.saveMsg(event.getChannel().getIdAsString(),Response);
            }
        }
    }
}