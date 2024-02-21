package catx.feitu.DiscordSelfClient.client.impl;

public class Friend {
    private String id;
    private int type;
    private String nickname;
    private String username;
    private String displayName;
    private String avatar;
    private String avatarDecoration;
    private String discriminator;
    private int publicFlags;

    public Friend(String _id, int _type, String _nickname, String _username, String _displayName, String _avatar, String
                  _avatarDecoration, String _discriminator, int _publicFlags) {
        id = _id;
        type = _type;
        nickname = _nickname;
        username = _username;
        displayName = _displayName;
        avatar = _avatar;
        avatarDecoration = _avatarDecoration;
        discriminator = _discriminator;
        publicFlags = _publicFlags;
    }

    public String id() {
        return id;
    }

    public int type() {
        return type;
    }

    public String nickname() {
        return nickname;
    }

    public String username() {
        return username;
    }

    public String displayname() {
        return displayName;
    }

    public String avatar() {
        return avatar;
    }

    public String avatar_decoration() {
        return avatarDecoration;
    }

    public String discriminator() {
        return discriminator;
    }

    public int public_flags() {
        return publicFlags;
    }
}
