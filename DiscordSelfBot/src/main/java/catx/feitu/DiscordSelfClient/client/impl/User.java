package catx.feitu.DiscordSelfClient.client.impl;

public class User {
    private final String id;
    private final String username;
    private final boolean bot;
    public User(String id ,String username ,boolean isBot){
        this.id = id;
        this.username = username;
        this.bot = isBot;
    }
    public String getId() {
        return id;
    }
    public String getUserName() {
        return username;
    }
    public boolean isBot() {
        return bot;
    }
}
