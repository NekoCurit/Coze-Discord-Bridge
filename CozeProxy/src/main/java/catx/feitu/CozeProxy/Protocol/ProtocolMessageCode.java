package catx.feitu.CozeProxy.Protocol;

public class ProtocolMessageCode {
    private String protocol = "";
    public ProtocolMessageCode(String protocol) {
        this.protocol = protocol;
    }
    public String mentionUser(String userID) {
        switch(protocol) {
            case catx.feitu.CozeProxy.Protocol.Protocols.DISCORD:
                return "<@" + userID + ">";
            case catx.feitu.CozeProxy.Protocol.Protocols.SLACK:
                return "@" + userID;
        }
        return "";
    }
}
