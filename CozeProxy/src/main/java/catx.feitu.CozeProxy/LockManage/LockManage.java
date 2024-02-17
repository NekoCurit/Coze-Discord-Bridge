package catx.feitu.CozeProxy.LockManage;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class LockManage {
    private final ConcurrentHashMap<Object, Lock> lockMap = new ConcurrentHashMap<>();
    public Lock getLock(String key) {
        return lockMap.computeIfAbsent(key, k -> new ReentrantLock());
    }
}
