package catx.feitu.CozeProxy.Protocol;

import java.util.List;

public class UniversalMessage {
    public String content;
    public List<String> files;
    public boolean hasButton;
    public UniversalMessage setContent (String content) {
        UniversalMessage build = this;
        build.content = content;
        return build;
    }
    public UniversalMessage setFiles (List<String> content) {
        UniversalMessage build = this;
        build.files = content;
        return build;
    }
    public UniversalMessage setHasButton (boolean hasButton) {
        UniversalMessage build = this;
        build.hasButton = hasButton;
        return build;
    }
}
