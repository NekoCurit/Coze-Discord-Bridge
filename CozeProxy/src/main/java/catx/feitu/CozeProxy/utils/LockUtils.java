package catx.feitu.CozeProxy.utils;

import catx.feitu.CozeProxy.data.datas.LockData;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class LockUtils {
    private final LockData data;
    public LockUtils (LockData _data) {
        data = _data;
    }

    public Lock getLock(String key) {
        return data.lockMap.computeIfAbsent(key, k -> new ReentrantLock());
    }
}
