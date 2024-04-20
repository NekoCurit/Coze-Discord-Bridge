package catx.feitu.CozeProxy.data.datas;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;

public class LockData {
    public ConcurrentHashMap<Object, Lock> lockMap = new ConcurrentHashMap<>();
}
