package catx.feitu.CozeProxy.FunctionalInterface;

@FunctionalInterface
public interface ChatStreamEvent {
    boolean handle(String ALLGenerateMessages, String NewGenerateMessage);
}
