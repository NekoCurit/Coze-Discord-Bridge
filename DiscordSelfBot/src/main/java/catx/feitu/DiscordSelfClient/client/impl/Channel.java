package catx.feitu.DiscordSelfClient.client.impl;

public class Channel {
    private final String id;
    private final int type;
    private final String lastMessageId;
    private final int flags;
    private final String name;

    public Channel(String _id, int _type, String _lastMessageId, int _flags, String name) {
        id = _id;
        type = _type;
        lastMessageId = _lastMessageId;
        flags = _flags;
        this.name = name;
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
    public String getName() {
        return name;
    }
}
