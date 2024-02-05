package catx.feitu.coze_discord_bridge;
import catx.feitu.coze_discord_bridge.Config.ConfigManage;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class KeepaliveTimer {
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    // 定义你的任务
    private final Runnable task = GPTManage::keepalive;
    public void start() {
        scheduler.scheduleAtFixedRate(task, 0, ConfigManage.Configs.Keepalive_timer, TimeUnit.MINUTES);
    }
    public void stop() {
        scheduler.shutdown();
    }
}
