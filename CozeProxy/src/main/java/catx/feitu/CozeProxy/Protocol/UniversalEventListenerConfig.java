package catx.feitu.CozeProxy.Protocol;


public class UniversalEventListenerConfig {
    public String filterServerID;
    public String filterUserID;
    public UniversalEventListenerConfig(String serverID, String userID) {
        filterServerID = serverID;
        filterUserID = userID;
    }
}
