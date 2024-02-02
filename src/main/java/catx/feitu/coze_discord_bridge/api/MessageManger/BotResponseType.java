package catx.feitu.coze_discord_bridge.api.MessageManger;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class BotResponseType {
    public String prompt = "";
    public List<String> files =new ArrayList<>();
    public long timestamp = Instant.now().toEpochMilli(); // 0 = 100% 完成标志
    public boolean IsCompleted (long timeout) {
        return this.timestamp == 0 ||
                Instant.now().toEpochMilli() - this.timestamp > timeout;
    }
    public void SetCompleted (boolean completed) {
       this.timestamp = completed ? Instant.now().toEpochMilli() : 0;
    }
}
