package catx.feitu.coze_discord_bridge.api.FunctionalInterface;

import java.io.IOException;

@FunctionalInterface
public interface ChatStreamEvent {
    boolean handle(String ALLGenerateMessages, String NewGenerateMessage);
}
