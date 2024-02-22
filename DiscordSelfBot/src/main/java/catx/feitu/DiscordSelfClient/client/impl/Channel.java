package catx.feitu.DiscordSelfClient.client.impl;

public class Channel {
    private String id;
    private int type;
    private String lastMessageId;
    private int flags;

    public Channel(String _id, int _type, String _lastMessageId, int _flags) {
        id = _id;
        type = _type;
        lastMessageId = _lastMessageId;
        flags = _flags;
    }

    public String id() {
        return id;
    }

    public int type() {
        return type;
    }

    public String last_message_id() {
        return lastMessageId;
    }

    public int flags() {
        return flags;
    }
}
