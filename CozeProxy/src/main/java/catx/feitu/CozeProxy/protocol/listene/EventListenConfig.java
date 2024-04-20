package catx.feitu.CozeProxy.protocol.listene;


public class EventListenConfig {
    public String filterServerID;
    public String filterUserID;
    public String filterSelfUserID;
    public boolean filterReply;
    public EventListenConfig(String serverID, String userID , boolean reply) {
        filterServerID = serverID;
        filterUserID = userID;
        filterReply = reply;
    }
}
