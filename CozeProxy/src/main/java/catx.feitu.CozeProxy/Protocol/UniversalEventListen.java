package catx.feitu.CozeProxy.Protocol;

public interface UniversalEventListen {

    void onUserStartTyping(String serverID ,String channelID ,String userID);
    void onMessageCreate(String serverID ,String channelID ,String userID ,UniversalMessage message);
    void onMessageEdit(String serverID ,String channelID ,String userID ,UniversalMessage message);

}