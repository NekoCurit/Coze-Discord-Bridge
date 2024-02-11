package catx.feitu.coze_discord_bridge.api.ConversationManage;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class ConversationHelper {
    public static String conversation2JsonString(ConversationData conversation) {
        return JSON.toJSONString(conversation.getMap());
    }
    public static ConversationData jsonString2Conversation(String jsonString) {
        TypeReference<ConcurrentHashMap<String, String>> typeRef = new TypeReference<ConcurrentHashMap<String, String>>() {};
        ConcurrentHashMap<String, String> conversation = JSON.parseObject(jsonString, typeRef);
        return new ConversationData(conversation);
    }
    public static List<String> conversationGetIdAsList (ConversationData conversation) {
        List<String> idList = Collections.synchronizedList(new ArrayList<>());
        idList.addAll(conversation.getMap().values());
        return idList;
    }
    public static List<String> conversationGetNameAsList (ConversationData conversation) {
        ConcurrentHashMap<String, String> map = conversation.getMap();
        return new ArrayList<>(map.keySet());
    }
}
