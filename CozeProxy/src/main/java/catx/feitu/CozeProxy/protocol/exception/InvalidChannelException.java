package catx.feitu.CozeProxy.protocol.exception;

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
