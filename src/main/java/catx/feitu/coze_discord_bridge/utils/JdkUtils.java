package catx.feitu.coze_discord_bridge.utils;

public class JdkUtils {
    public static int getJdkVersion() {
        String version = System.getProperty("java.version");

        if (version.startsWith("1.")) {
            version = version.substring(2, 3);
        } else {
            int dot = version.indexOf(".");
            if (dot != -1) { version = version.substring(0, dot); }
        }

        return Integer.parseInt(version);
    }
}
