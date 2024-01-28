package catx.feitu.coze_discord_bridge.Misc;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class LockManager {
    private static final ConcurrentHashMap<Object, Lock> lockMap = new ConcurrentHashMap<>();
    public static Lock getLock(String key) {
        return lockMap.computeIfAbsent(key, k -> new ReentrantLock());
    }
}
