package catx.feitu.CozeProxy.exception;

public class InvalidDiscordChannelException extends Exception {
    private String Prv_Conversation = "";
    public InvalidDiscordChannelException(String Conversation) {
        super(Conversation);
        Prv_Conversation = Conversation;
    }
    public String Get_Conversation() {
        return Prv_Conversation;
    }
}
