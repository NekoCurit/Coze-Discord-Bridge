package catx.feitu.coze_discord_bridge.api.Exceptions;

public class SendMessageException extends Exception {
    public SendMessageException() {
        super();
    }
    public SendMessageException(String Message) {
        super(Message);
    }
    public SendMessageException(String message, Throwable cause) {
        super(message, cause);
    }
}
