package catx.feitu.coze_discord_bridge.api.ConversationManage;

import java.util.concurrent.ConcurrentHashMap;

public class ConversationData {
    public ConcurrentHashMap<String, String> conversations = new ConcurrentHashMap<>();
    public ConversationData() { }
    public ConversationData(ConcurrentHashMap<String, String> conversations) {
        this.conversations = conversations;
    }
    public void put(String name,String conversationID) {
        conversations.put(name, conversationID);
    }

    public void remove(String name) {
        conversations.remove(name);
    }
    public String get(String name) {
        return conversations.getOrDefault(name, name); // 索引为空那么传入的可能是频道ID 直接返回
    }
    public ConcurrentHashMap<String, String> getMap() {
        return conversations;
    }
}
