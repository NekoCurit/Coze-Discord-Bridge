package catx.feitu.DiscordSelfClient.client.impl;

import java.util.List;

public class Message {
    private final String id;
    private final int type;
    private final String content;
    private final User user;
    private final List<Attachment> attachments;
    private final boolean hasComponents;


    public Message(String id, int type, String content , User user , List<Attachment> attachments ,boolean hasComponents) {
        this.id = id;
        this.type = type;
        this.content = content;
        this.user = user;
        this.attachments = attachments;
        this.hasComponents = hasComponents;
    }

    public String getId() {
        return id;
    }

    public int getType() {
        return type;
    }

    public User getUser() {
        return user;
    }
    public String getContent() {
        return content;
    }
    public List<Attachment> getAttachments() {
        return attachments;
    }

    public boolean isHasComponents() {
        return hasComponents;
    }
}
