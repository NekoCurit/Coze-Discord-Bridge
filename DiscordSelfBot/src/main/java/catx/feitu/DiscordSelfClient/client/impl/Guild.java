package catx.feitu.DiscordSelfClient.client.impl;

public class Guild {
    private String id;
    private String name;
    private String icon;
    private boolean owner;
    private String permissions;

    public Guild(String _id, String _name, String _icon, boolean _owner, String _permissions) {
        id = _id;
        name = _name;
        icon = _icon;
        owner = _owner;
        permissions = _permissions;
    }

    public String id() {
        return id;
    }

    public String name() {
        return name;
    }

    public String icon() {
        return icon;
    }

    public boolean owner() {
        return owner;
    }

    public String permissions() {
        return permissions;
    }
}
