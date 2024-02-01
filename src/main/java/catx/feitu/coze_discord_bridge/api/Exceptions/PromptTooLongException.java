package catx.feitu.coze_discord_bridge.api.Exceptions;

public class PromptTooLongException extends Exception {
    private String Prv_Prompt = "";
    public PromptTooLongException(String Prompt) {
        super();
        Prv_Prompt = Prompt;
    }
    public long GetPromptLength () {
        return Prv_Prompt.length();
    }
    public String GetPrompt () {
        return Prv_Prompt;
    }
}
