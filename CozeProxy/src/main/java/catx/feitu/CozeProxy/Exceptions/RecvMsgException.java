package catx.feitu.CozeProxy.Exceptions;

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
