package catx.feitu.CozeProxy.listen;

import catx.feitu.CozeProxy.impl.response.Response;
import catx.feitu.CozeProxy.protocol.listene.EventListene;
import catx.feitu.CozeProxy.protocol.message.MessageBuilder;
import catx.feitu.CozeProxy.utils.Utils;

import java.time.Instant;

public class EventHandle implements EventListene {
    private final Utils utils;
    public EventHandle(Utils _utils) {
        utils = _utils;
    }

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
        utils.generateStatus.saveGenerateStatus(channelID);
        latestReceiveCozeMessage = Instant.now();
    }
    @Override
    public void onMessageStream(String channelID , MessageBuilder message) {
        Response responseType = new Response();

        responseType.prompt = message.content;
        responseType.files = message.files;
        responseType.SetCompleted(message.hasButton);

        utils.response.saveMsg(channelID ,responseType);
        latestReceiveCozeMessage = Instant.now();
    }
    public Instant getLatestSendMessageInstant() { return latestSendMessage; }
    public Instant getLatestReceiveMessageInstant() { return latestReceiveCozeMessage; }
}
