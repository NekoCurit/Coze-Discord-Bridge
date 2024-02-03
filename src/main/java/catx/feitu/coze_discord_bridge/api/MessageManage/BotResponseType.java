package catx.feitu.coze_discord_bridge.api.MessageManage;

import catx.feitu.coze_discord_bridge.api.CozeGPT;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class BotResponseType {
    public String prompt = "";
    public List<String> files =new ArrayList<>();
    public long timestamp = -1;
    public boolean IsCompleted (long timeout) {
        return (this.timestamp == 0 ||
                Instant.now().toEpochMilli() - this.timestamp > timeout) && this.timestamp != -1;
    }
    public void SetCompleted (boolean completed) {
       this.timestamp = completed ? 0 : Instant.now().toEpochMilli();
    }
}
