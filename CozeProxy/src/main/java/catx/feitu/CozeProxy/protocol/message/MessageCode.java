package catx.feitu.CozeProxy.protocol.message;

public class MessageCode {
    private String protocol = "";
    public MessageCode(String protocol) {
        this.protocol = protocol;
    }
    public String mentionUser(String userID) {
        switch(protocol) {
            case catx.feitu.CozeProxy.protocol.Protocols.DISCORD:
                return "<@" + userID + ">";
        }
        return "";
    }
}
