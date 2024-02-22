package catx.feitu.DiscordSelfClient.client.Types;

public class DiscordAttachment {
    public String fileName = "";
    public int id = 0;
    public String uploaded_filename = "";
    public DiscordAttachment (String fileName ,int id ,String uploaded_filename) {
        this.fileName = fileName;
        this.id = id;
        this.uploaded_filename = uploaded_filename;
    }
    public String toString () {
        return uploaded_filename;
    }
}
