package catx.feitu.CozeProxy.utils;

import catx.feitu.CozeProxy.data.Data;

public class Utils {
    private Data data;
    public Utils(Data _data) {
        data = _data;

        lock = new LockUtils(data.locks);
        conversation = new ConversationUtils(data.conversations);
        generateStatus = new GenerateStatusUtils(data.generateStatus);
        response = new ResponseUtils(data.responses);
    }
    public LockUtils lock;
    public ConversationUtils conversation;
    public GenerateStatusUtils generateStatus;
    public ResponseUtils response;
}
