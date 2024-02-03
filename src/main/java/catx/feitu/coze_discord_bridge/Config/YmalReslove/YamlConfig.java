package catx.feitu.coze_discord_bridge.Config.YmalReslove;

import com.esotericsoftware.yamlbeans.YamlException;
import com.esotericsoftware.yamlbeans.YamlReader;
import com.esotericsoftware.yamlbeans.YamlWriter;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

public class YamlConfig {
    public YamlConfig() {
    }

    public static String saveToString(Object o) throws YamlException {
        com.esotericsoftware.yamlbeans.YamlConfig yamlConfig = new com.esotericsoftware.yamlbeans.YamlConfig();
        yamlConfig.writeConfig.setWriteDefaultValues(true);
        yamlConfig.writeConfig.setEscapeUnicode(false);
        yamlConfig.writeConfig.setWriteRootElementTags(false);
        yamlConfig.writeConfig.setWriteRootTags(false);
        yamlConfig.writeConfig.setUseVerbatimTags(false);
        yamlConfig.writeConfig.setWriteClassname(com.esotericsoftware.yamlbeans.YamlConfig.WriteClassName.NEVER);
        StringWriter stringWriter = new StringWriter();
        YamlWriter yamlWriter = new YamlWriter(stringWriter, yamlConfig);
        yamlWriter.write(o);
        yamlWriter.close();
        return stringWriter.toString();
    }

    public static <T> T loadFromString(String s, Class<T> Class) throws IOException {
        YamlReader yamlReader = new YamlReader(new StringReader(s));
        T o = yamlReader.read(Class);
        yamlReader.close();
        return o;
    }

    public static <T> T loadFromFile(File file, T defaults) throws IOException {
        if (file.exists()) {
            return loadFromString(ReadFile.readFormFile(file), (Class<T>) defaults.getClass());
        } else {
            //ReadFile.WriteToFile(saveToString(defaults), file);
            return defaults;
        }
    }
}
