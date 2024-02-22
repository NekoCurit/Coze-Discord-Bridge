package catx.feitu.CozeProxy.Types;

import java.io.ByteArrayInputStream;
import java.util.Base64;

public class GPTFile {
    private final ByteArrayInputStream file;
    private final String name;
    public GPTFile(String base64, String fileName) {
        file = new ByteArrayInputStream(Base64.getDecoder().decode(base64));
        name = fileName;
    }
    public GPTFile(byte[] bytes, String fileName) {
        file = new ByteArrayInputStream(bytes);
        name = fileName;
    }
    public GPTFile(ByteArrayInputStream fileInputStream, String fileName) {

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
