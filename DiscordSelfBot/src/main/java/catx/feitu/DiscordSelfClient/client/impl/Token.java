package catx.feitu.DiscordSelfClient.client.impl;

public class Token {
    private String id;
    private String username;
    private String displayName;
    private String avatar;
    private String avatarDecoration;
    private String discriminator;
    private int publicFlags;
    private int flags;
    private String banner;
    private String bannerColor;
    private int accentColor;
    private String bio;
    private String locale;
    private boolean nsfw;
    private boolean mfa;
    private int premiumType;
    private String email;
    private boolean verified;
    private String phone;

    public Token(String _id, String _username, String _displayName, String _avatar, String _avatarDecoration,
                 String _discriminator, int _publicFlags, int _flags, String _banner, String _bannerColor, int _accentColor,
                 String _bio, String _locale, boolean _nsfw, boolean _mfa, int _premiumType, String _email, boolean _verified,
                 String _phone) {
        id = _id;
        username = _username;
        displayName = _displayName;
        avatar = _avatar;
        avatarDecoration = _avatarDecoration;
        discriminator = _discriminator;
        publicFlags = _publicFlags;
        flags = _flags;
        banner = _banner;
        bannerColor = _bannerColor;
        accentColor = _accentColor;
        bio = _bio;
        locale = _locale;
        nsfw = _nsfw;
        mfa = _mfa;
        premiumType = _premiumType;
        email = _email;
        verified = _verified;
        phone = _phone;
    }

    public String id() {
        return id;
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

    public int flags() {
        return flags;
    }

    public String banner() {
        return banner;
    }

    public String banner_color() {
        return bannerColor;
    }

    public int accent_color() {
        return accentColor;
    }

    public String bio() {
        return bio;
    }

    public String locale() {
        return locale;
    }

    public boolean nsfw() {
        return nsfw;
    }

    public boolean mfa() {
        return mfa;
    }

    public int premium_type() {
        return premiumType;
    }

    public String email() {
        return email;
    }

    public boolean verified() {
        return verified;
    }

    public String phone() {
        return phone;
    }
}
