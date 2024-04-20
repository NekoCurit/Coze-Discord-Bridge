package catx.feitu.CozeProxy.protocol.message;

import java.util.List;

public class MessageBuilder {
    public String content;
    public List<String> files;
    public boolean hasButton;
    public MessageBuilder setContent (String content) {
        MessageBuilder build = this;
        build.content = content;
        return build;
    }
    public MessageBuilder addContent (String content) {
        MessageBuilder build = this;
        build.content += content;
        return build;
    }
    public MessageBuilder setFiles (List<String> content) {
        MessageBuilder build = this;
        build.files = content;
        return build;
    }
    public MessageBuilder setHasButton (boolean hasButton) {
        MessageBuilder build = this;
        build.hasButton = hasButton;
        return build;
    }
}
