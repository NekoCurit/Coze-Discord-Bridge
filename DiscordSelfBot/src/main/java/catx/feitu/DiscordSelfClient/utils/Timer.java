package catx.feitu.DiscordSelfClient.utils;

public class Timer {
    private long lastMS = 0L;

    public int convertToMS(int d) {
        return 1000 / d;
    }

    public long getCurrentMS() {
        return System.nanoTime() / 1000000L;
    }

    public boolean hasReached(long milliseconds) {
        return getCurrentMS() - lastMS >= milliseconds;
    }

    public long getDelay() {
        return System.currentTimeMillis() - lastMS;
    }

    public long getDifference()
    {
        return getCurrentMS() - lastMS;
    }

    public void reset() {
        lastMS = getCurrentMS();
    }

    public void setLastMS() {
        lastMS = System.currentTimeMillis();
    }

    public void setLastMS(long lastMS) {
        this.lastMS = lastMS;
    }

    public boolean hasTimeElapsed(long time, boolean reset) {

        if (lastMS > System.currentTimeMillis()) {
            lastMS = System.currentTimeMillis();
        }

        if (System.currentTimeMillis() - lastMS > time) {
            if (reset)
                reset();

            return true;

        } else {
            return false;
        }

    }
}