package catx.feitu.CozeProxy.exception;

public class InvalidConversationException extends Exception {
    private String Prv_Conversation = "";
    public InvalidConversationException(String Conversation) {
        super(Conversation);
        Prv_Conversation = Conversation;
    }
    public String Get_Conversation() {
        return Prv_Conversation;
    }
}
