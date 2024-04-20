package catx.feitu.CozeProxy.data;

import catx.feitu.CozeProxy.data.datas.ConversationData;
import catx.feitu.CozeProxy.data.datas.GenerateStatusData;
import catx.feitu.CozeProxy.data.datas.LockData;
import catx.feitu.CozeProxy.data.datas.ResponseData;

public class Data {
    public ConversationData conversations = new ConversationData();
    public ResponseData responses = new ResponseData();
    public LockData locks = new LockData();
    public GenerateStatusData generateStatus = new GenerateStatusData();
}
