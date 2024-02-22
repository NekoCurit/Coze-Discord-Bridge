package catx.feitu.CozeProxy.Protocol.Exception;

public class InvalidChannelException extends Exception {
    private String channelID;
    public InvalidChannelException(String channelID) {
        super(channelID);
        this.channelID = channelID;
    }
    public String getChannelID() {
        return channelID;
    }
}
