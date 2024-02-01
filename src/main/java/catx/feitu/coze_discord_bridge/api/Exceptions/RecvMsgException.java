package catx.feitu.coze_discord_bridge.api.Exceptions;

public class RecvMsgException extends Exception {
    public RecvMsgException() {
        super();
    }
    public RecvMsgException(String Message) {
        super(Message);
    }
    public RecvMsgException(String message, Throwable cause) {
        super(message, cause);
    }
}
