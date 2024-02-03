package catx.feitu.coze_discord_bridge.api.ConversationManage;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;

import java.util.concurrent.ConcurrentHashMap;

public class ConversationHelper {
    public static String Conversation2JsonString (ConversationData conversation) {
        return JSON.toJSONString(conversation.getMap());
    }
    public static ConversationData JsonString2Conversation (String jsonString) {
        TypeReference<ConcurrentHashMap<String, String>> typeRef = new TypeReference<ConcurrentHashMap<String, String>>() {};
        ConcurrentHashMap<String, String> conversation = JSON.parseObject(jsonString, typeRef);
        return new ConversationData(conversation);
    }
}
