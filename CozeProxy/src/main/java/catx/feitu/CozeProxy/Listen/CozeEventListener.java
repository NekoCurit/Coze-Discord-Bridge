package catx.feitu.CozeProxy.Listen;

import catx.feitu.CozeProxy.MessageManage.BotGenerateStatusManage;
import catx.feitu.CozeProxy.MessageManage.BotResponseManage;
import catx.feitu.CozeProxy.MessageManage.BotResponseType;
import catx.feitu.CozeProxy.Protocol.UniversalEventListener;
import catx.feitu.CozeProxy.Protocol.UniversalMessage;

import java.time.Instant;

public class CozeEventListener implements UniversalEventListener {
    public BotGenerateStatusManage botGenerateStatusManage = new BotGenerateStatusManage();
    public BotResponseManage botResponseManage = new BotResponseManage();
    /**
     * 最后一次发送消息时间
     */
    private Instant latestSendMessage = Instant.now();
    /**
     * 最后一次接收消息时间
     */
    private Instant latestReceiveCozeMessage = Instant.now();

    @Override
    public void onSelfMessageSend(String channelID) {
        latestSendMessage = Instant.now();
    }
    @Override
    public void onStartGenerate(String channelID) {
        botGenerateStatusManage.saveGenerateStatus(channelID);
        latestReceiveCozeMessage = Instant.now();
    }
    @Override
    public void onMessageStream(String channelID ,UniversalMessage message) {
        BotResponseType responseType = new BotResponseType();
        responseType.prompt = message.content;
        responseType.files = message.files;
        responseType.SetCompleted(message.hasButton);
        botResponseManage.saveMsg(channelID ,responseType);
        latestReceiveCozeMessage = Instant.now();
    }
    public Instant getLatestSendMessageInstant() { return latestSendMessage; }
    public Instant getLatestReceiveMessageInstant() { return latestReceiveCozeMessage; }
}
