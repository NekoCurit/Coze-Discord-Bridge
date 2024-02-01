package catx.feitu.coze_discord_bridge.Config;

public class ConfigData {

        public String Discord_Bot_Token = "";
        public String Discord_CreateChannel_Category = "";

        public Boolean UsingProxy = false;

        public String ProxyType = "HTTP";
        public String ProxyIP = "127.0.0.1";
        public int ProxyPort = 8080;

        public String CozeBot_id = "";
        public String CozeBot_InServer_id = "";

        public long generate_timeout = 2000;

        public boolean Disable_CozeBot_ReplyMsgCheck = false;
        public boolean Disable_Name_Cache = false;

        public int APIPort = 8092;
        public int APISSLPort = 8093;
        public String APISSL_keyStorePath = "";
        public String APISSL_keyStorePassword = "";
        public String APIKey = "";

        public int OpenAPI_Chat_Default_Models2Conversation = 0;
        public String OpenAPI_Chat_Default_Channel = "default";
        public String OpenAPI_Chat_MsgForward_Prefix = "";
        public String OpenAPI_Chat_MsgForward_Suffix = "";
        public String OpenAPI_ImageGenerate_Default_Channel = "";

}
