package catx.feitu.CozeProxy.Protocol;


public class UniversalEventListenerConfig {
    public String filterServerID;
    public String filterUserID;
    public String filterSelfUserID;
    public boolean filterReply;
    public UniversalEventListenerConfig(String serverID, String userID , boolean reply) {
        filterServerID = serverID;
        filterUserID = userID;
        filterReply = reply;
    }
}
