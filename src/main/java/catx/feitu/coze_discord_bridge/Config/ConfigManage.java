package catx.feitu.coze_discord_bridge.Config;

import catx.feitu.coze_discord_bridge.Config.YmalReslove.YamlConfig;
import catx.feitu.coze_discord_bridge.Main;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

public class ConfigManage {
    public static ConfigData configs;
    private static final Logger logger = LogManager.getLogger(ConfigManage.class);


    public static void ReadConfig () {
        try {
            Configs = YamlConfig.loadFromFile(new File("Config.yml"), new ConfigData());
            logger.info("加载配置成功");
        } catch (Exception e) {
            logger.warn("加载配置失败",e);
            configs = configs == null ? new ConfigData() : configs;
        }
    }
    public static void DefaultConfig() {
        if(Files.exists(Paths.get("Config.yml"))) {
            return;
        }
        try (InputStream is = Main.class.getResourceAsStream("/Config.yml");
             OutputStream os = new FileOutputStream("Config.yml")) {
            if (is == null) {
                throw new IllegalArgumentException("找不到jar内资源文件:Config.yml");
            }
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);

            }
        } catch (Exception e) {
            logger.warn("写出默认配置失败",e);
        }
    }
    public static void SaveConfig() {
        try {
            Files.writeString(new File("Config.yml").toPath(), YamlConfig.saveToString(Configs));
            logger.info("保存配置成功");
        } catch (Exception e) {
            logger.warn("保存配置失败",e);
        }
    }
}
