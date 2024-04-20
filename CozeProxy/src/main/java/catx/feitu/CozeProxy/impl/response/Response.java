package catx.feitu.CozeProxy.impl.response;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class Response {
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
