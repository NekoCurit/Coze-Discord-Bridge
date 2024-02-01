package catx.feitu.coze_discord_bridge.api.FunctionalInterface;

@FunctionalInterface
public interface ChatStreamEvent {
    void handle(String ALLGenerateMessages, String NewGenerateMessage);
}
