package catx.feitu.coze_discord_bridge.api.Types;

import java.io.*;
import java.util.Base64;

public class GPTFiles {
    private final ByteArrayInputStream file;
    private final String name;
    public GPTFiles(String base64, String fileName) {
        file = new ByteArrayInputStream(Base64.getDecoder().decode(base64));
        name = fileName;
    }
    public GPTFiles(byte[] bytes, String fileName) {
        file = new ByteArrayInputStream(bytes);
        name = fileName;
    }
    public GPTFiles(ByteArrayInputStream fileInputStream, String fileName) {

        file = fileInputStream;
        name = fileName;
    }
    public ByteArrayInputStream GetByteArrayInputStream() {
        return file;
    }
    public String GetFileName() {
        return name;
    }
}
