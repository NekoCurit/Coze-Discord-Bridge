package catx.feitu.DiscordSelfClient.client.Exceptions;

public class InvalidTokenException extends Exception {
    public String token = "";
    public InvalidTokenException (String token) {
        this.token = token;
    }
}
