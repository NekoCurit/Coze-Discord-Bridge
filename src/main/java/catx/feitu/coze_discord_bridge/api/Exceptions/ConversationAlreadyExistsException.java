package catx.feitu.coze_discord_bridge.api.Exceptions;

public class ConversationAlreadyExistsException extends Exception {
    private String Prv_Conversation = "";
    public ConversationAlreadyExistsException(String Conversation) {
        super(Conversation);
        Prv_Conversation = Conversation;
    }
    public String Get_Conversation() {
        return Prv_Conversation;
    }
}
