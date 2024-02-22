package catx.feitu.DiscordSelfClient.client.impl;

public class Attachment {
    private final String id;
    private final String filename;
    private final String proxy_url;
    private final String url;
    public Attachment(String id ,String filename ,String proxy_url ,String url) {
        this.id = id;
        this.filename = filename;
        this.proxy_url = proxy_url;
        this.url = url;
    }
    public String getUrl() {
        return url;
    }

}
