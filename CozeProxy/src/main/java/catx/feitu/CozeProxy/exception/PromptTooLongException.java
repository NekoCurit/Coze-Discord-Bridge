package catx.feitu.CozeProxy.exception;

public class PromptTooLongException extends Exception {
    private String Prv_Prompt = "";
    private long Prv_Limit = 0;
    public PromptTooLongException(String Prompt,long Limit) {
        super();
        Prv_Prompt = Prompt;
        Prv_Limit = Limit;
    }
    public long GetPromptLength () {
        return Prv_Prompt.length();
    }
    public long GetLimitLength () {
        return Prv_Limit;
    }
    public String GetPrompt () {
        return Prv_Prompt;
    }
}
