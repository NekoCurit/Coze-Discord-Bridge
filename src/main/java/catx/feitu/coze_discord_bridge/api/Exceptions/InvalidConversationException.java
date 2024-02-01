package catx.feitu.coze_discord_bridge.api.Exceptions;

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
