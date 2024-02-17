package catx.feitu.CozeProxy.Listen;

import catx.feitu.CozeProxy.CozeGPTConfig;
import catx.feitu.CozeProxy.MessageManage.BotGenerateStatusManage;
import catx.feitu.CozeProxy.MessageManage.BotResponseManage;
import catx.feitu.CozeProxy.MessageManage.BotResponseType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.javacord.api.entity.message.embed.Embed;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.event.message.MessageEditEvent;
import org.javacord.api.event.user.UserStartTypingEvent;
import org.javacord.api.listener.message.MessageCreateListener;
import org.javacord.api.listener.message.MessageEditListener;
import org.javacord.api.listener.user.UserStartTypingListener;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MessageListener implements MessageCreateListener, MessageEditListener, UserStartTypingListener {
    private static final Logger logger = LogManager.getLogger(MessageListener.class);
    private final BotResponseManage botResponseManage;
    private final BotGenerateStatusManage botGenerateStatusManage;
    private final CozeGPTConfig config;
    /**
     * 最后一次发送消息时间
     */
    private Instant latestSendMessage = Instant.now();
    /**
     * 最后一次接收消息时间
     */
    private Instant latestReceiveCozeMessage = Instant.now();
    public MessageListener(BotResponseManage botResponseManage, BotGenerateStatusManage botGenerateStatusManage, CozeGPTConfig config) {
        this.botResponseManage = botResponseManage;
        this.botGenerateStatusManage = botGenerateStatusManage;
        this.config = config;
    }

    @Override
    public void onUserStartTyping(UserStartTypingEvent event) {
        if(Objects.equals(event.getUserIdAsString(), config.CozeBot_id)) {
            this.botGenerateStatusManage.saveGenerateStatus(event.getChannel().getIdAsString());
            logger.info("[CozeBot Start Generate] " + event.getChannel().getIdAsString());
        }
    }
    @Override
    public void onMessageCreate(MessageCreateEvent event) {
        latestSendMessage = Instant.now();
        if (Objects.equals(event.getMessageAuthor().getIdAsString(), event.getApi().getYourself().getIdAsString())) {
            logger.info("[Send] " + event.getChannel().getIdAsString() + ": " + event.getMessageContent());
        }
    }

    @Override
    public void onMessageEdit(MessageEditEvent event) {
        if (Objects.equals(event.getMessageAuthor().getIdAsString(), config.CozeBot_id)) {
            latestReceiveCozeMessage = Instant.now();
            if (config.Disable_CozeBot_ReplyMsgCheck || event.getMessage().getMentionedUsers().contains(event.getApi().getYourself())) {
                boolean Done100 = !event.getMessage().getComponents().isEmpty(); //存在按钮 = 100%响应完毕
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
                this.botResponseManage.saveMsg(event.getChannel().getIdAsString(),Response);
            }
        }
    }
    /**
     * 取出最后一次发送消息时间
     * @return 返回 Instant 类型
     */
    public Instant getLatestSendMsgInstant () {
        return latestSendMessage;
    }
    /**
     * 取出最后一次接收Coze Bot消息时间
     * @return 返回 Instant 类型
     */
    public Instant getLatestReceiveCozeMsgInstant () {
        return latestReceiveCozeMessage;
    }
}