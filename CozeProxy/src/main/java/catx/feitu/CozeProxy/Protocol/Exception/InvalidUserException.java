package catx.feitu.CozeProxy.Protocol.Exception;

public class InvalidUserException extends Exception {
    private String userID;
    public InvalidUserException(String userID) {
        super(userID);
        this.userID = userID;
    }
    public String getUserID() {
        return userID;
    }
}
