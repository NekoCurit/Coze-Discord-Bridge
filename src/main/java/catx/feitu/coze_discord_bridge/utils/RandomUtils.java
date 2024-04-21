package catx.feitu.coze_discord_bridge.utils;

public class RandomUtils {
    public static String RandomName() {
        java.util.Random random = new java.util.Random();
        StringBuilder sb = new StringBuilder(16);
        String characters = "0123456789abcdef";
        for(int i=0; i<16; i++){
            int index = random.nextInt(characters.length());
            sb.append(characters.charAt(index));
        }
        return(sb.toString());
    }
}
