package catx.feitu.CozeProxy.Protocol;

public interface UniversalEventListener {
    void onSelfMessageSend(String channelID);
    void onStartGenerate(String channelID);
    void onMessageStream(String channelID ,UniversalMessage message);

}