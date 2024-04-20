package catx.feitu.CozeProxy.protocol.listene;

import catx.feitu.CozeProxy.protocol.message.MessageBuilder;

public interface EventListene {
    void onSelfMessageSend(String channelID);
    void onStartGenerate(String channelID);
    void onMessageStream(String channelID , MessageBuilder message);

}