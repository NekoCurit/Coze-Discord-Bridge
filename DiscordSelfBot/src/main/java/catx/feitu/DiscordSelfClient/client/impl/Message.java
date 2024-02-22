package catx.feitu.DiscordSelfClient.client.impl;

public class Message {
    private String id;
    private int type;
    private String content;


    public Message(String _id, int _type, String _content) {
        id = _id;
        type = _type;
        content = _content;
    }

    public String id() {
        return id;
    }

    public int type() {
        return type;
    }

    public String content() {
        return content;
    }
}
