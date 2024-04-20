package catx.feitu.CozeProxy.data.datas;

import catx.feitu.CozeProxy.impl.response.Response;
import java.util.concurrent.ConcurrentHashMap;

public class ResponseData {
    public ConcurrentHashMap<Object, Response> ResponseMap = new ConcurrentHashMap<>();
}
