package catx.feitu.CozeProxy.protocol.listene;


public class EventListeneConfig {
    public String filterServerID;
    public String filterUserID;
    public String filterSelfUserID;
    public boolean filterReply;
    public EventListeneConfig(String serverID, String userID , boolean reply) {
        filterServerID = serverID;
        filterUserID = userID;
        filterReply = reply;
    }
}
