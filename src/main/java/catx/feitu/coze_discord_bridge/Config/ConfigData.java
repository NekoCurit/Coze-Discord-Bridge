package catx.feitu.coze_discord_bridge.Config;

public class ConfigData {

        public String Discord_Bot_Token = "";

        public Boolean UsingProxy = false;

        public String ProxyType = "HTTP";
        public String ProxyIP = "127.0.0.1";
        public int ProxyPort = 8080;

        public String CozeBot_id = "";
        public String CozeBot_InServer_id = "";

        public long generate_timeout = 2000;

        public boolean Ignore_CozeBot_ReplyMsgCheck = false;
        public boolean Disable_Name_Cache = false;

        public int APIPort = 8092;
        public String APIKey = "";
}