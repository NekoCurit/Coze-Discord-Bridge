package catx.feitu.CozeProxy.exception;

public class InvalidConfigException extends Exception {
    private String Prv_Config_name = "";
    private String Prv_message = "";
    public InvalidConfigException(String Config_name,String message) {
        super(Config_name + ":" + message);
        Prv_Config_name = Config_name;
        Prv_message = message;
    }
    public InvalidConfigException(String Config_name) {
        super(Config_name);
        Prv_Config_name = Config_name;
    }
    public String Get_Invalid_ConfigName() {
        return Prv_Config_name;
    }
    public String Get_message() {
        return Prv_message;
    }
}
