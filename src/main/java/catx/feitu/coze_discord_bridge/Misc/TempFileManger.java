package catx.feitu.coze_discord_bridge.Misc;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class TempFileManger {
    private static final Logger logger = LogManager.getLogger(TempFileManger.class);

    public static void init() {
        File tempDir = new File("tmp");
        if (!tempDir.exists()) {
            if (!tempDir.mkdir()) {
                logger.error("创建临时目录失败 上传图片/长文本功能将不可用",new Exception());
            } else {
                logger.info("创建临时目录成功");
            }
        }
    }
    public static File fwrite_custom(byte[] Bytes,String Suffix) throws Exception {
        File outputFile = new File("/tmp/" + byte2MD5(Bytes) + "." + Suffix);
        try (OutputStream outputStream = new FileOutputStream(outputFile)) {
            outputStream.write(Bytes);
        }
        return outputFile;
    }
    public static File fwrite_base64(String base64) throws Exception {
        return fwrite_custom(Base64.getDecoder().decode(base64),"png");
    }
    public static File fwrite_String(String txt) throws Exception {
        return fwrite_custom(txt.getBytes(StandardCharsets.UTF_8),"txt");
    }


    public static String byte2MD5(byte[] inputBytes) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] digest = md.digest(inputBytes);
        StringBuilder sb = new StringBuilder();
        for (byte b : digest) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
