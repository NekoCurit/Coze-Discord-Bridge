package catx.feitu.CozeProxy.Interface;

@FunctionalInterface
public interface ChatStreamEvent {
    boolean handle(String ALLGenerateMessages, String NewGenerateMessage);
}
