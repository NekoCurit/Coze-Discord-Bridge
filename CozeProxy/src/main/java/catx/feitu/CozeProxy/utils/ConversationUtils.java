package catx.feitu.CozeProxy.utils;

import catx.feitu.CozeProxy.data.datas.ConversationData;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class ConversationUtils {
    private final ConversationData conversations;

    public ConversationUtils(ConversationData _conversations) {
        conversations = _conversations;
    }
    public void put(String name,String conversationID) {
        if (name == null || conversationID == null) { return; }
        conversations.conversations.put(name, conversationID);
    }

    public void remove(String name) {
        conversations.conversations.remove(name);
    }
    public String getId(String name) { return conversations.conversations.getOrDefault(name, name); // 索引为空那么传入的可能是频道ID 直接返回
    }
    public String getName(String Id) {
        for (String key : conversations.conversations.keySet()) {
            if (conversations.conversations.get(key).equals(Id)) {
                return key; // 返回寻找到的第一个
            }
        }
        return null;
    }
    public void SetMap(ConcurrentHashMap<String, String> _conversations) {
        conversations.conversations = _conversations;
    }
    public ConcurrentHashMap<String, String> getMap() {
        return conversations.conversations;
    }




    public static String conversation2JsonString(ConversationData conversation) {
        return JSON.toJSONString(conversation.conversations);
    }
    public static ConversationData jsonString2Conversation(String jsonString) {
        TypeReference<ConcurrentHashMap<String, String>> typeRef = new TypeReference<>() {};

        ConversationData data = new ConversationData();
        data.conversations = JSON.parseObject(jsonString, typeRef);

        return data;
    }
    public static List<String> conversationGetIdAsList (ConversationUtils conversation) {
        List<String> idList = Collections.synchronizedList(new ArrayList<>());
        idList.addAll(conversation.getMap().values());
        return idList;
    }
    public static List<String> conversationGetNameAsList (ConversationUtils conversation) {
        ConcurrentHashMap<String, String> map = conversation.getMap();
        return new ArrayList<>(map.keySet());
    }
}
