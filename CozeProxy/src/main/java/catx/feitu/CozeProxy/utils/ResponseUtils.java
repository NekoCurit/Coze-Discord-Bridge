package catx.feitu.CozeProxy.utils;

import catx.feitu.CozeProxy.data.datas.ResponseData;
import catx.feitu.CozeProxy.impl.response.Response;

import java.util.concurrent.ConcurrentHashMap;

public class ResponseUtils {
    private final ResponseData data;
    public ResponseUtils (ResponseData _data) {
        data = _data;
    }
    public void saveMsg(String channelID, Response response) {
        data.ResponseMap.put(channelID, response);
    }
    public Response getMsg(String channelID) throws NullPointerException {
        if (!data.ResponseMap.containsKey(channelID)) {
            throw new NullPointerException();
        }
        return data.ResponseMap.get(channelID);
    }
    public void clearMsg(String channelID) {
        data.ResponseMap.remove(channelID);
    }

}
