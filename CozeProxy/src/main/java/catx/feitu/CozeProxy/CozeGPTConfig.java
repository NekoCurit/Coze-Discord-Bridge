package catx.feitu.CozeProxy;

public class CozeGPTConfig {
    public String loginApp = "";
    /**
     * #Discord bot token 获取方法
     * 浏览器打开 <a href="https://discord.com/developers/">Discord开发者平台</a>
     * 创建Application > 点击Bot > 点击 Reset Token 然后复制过来即可
     * 注意 还需要打开Privileged Gateway Intents下面的选项 (MESSAGE CONTENT INTENT一定要开)
     */
    public String token = "";
    /**
     * 连接到Discord服务器时所使用的代理
     * 对于某些无法直连Discord的地区很有帮助
     * 设置为 null 则不适用
     */
    public java.net.Proxy Proxy = null;
    /**
     * Discord服务器ID
     * 打开Discord开发者模式后直接右键右侧服务器列表中Bot所在且有管理权限的服务器即可复制ID
     */
    public String serverID = "";
    /**
     * Discord中被Coze托管的机器人用户ID
     * 打开Discord开发者模式后在服务器内直接右键机器人即可复制ID
     */
    public String botID = "";

    public String Discord_CreateChannel_Category = "";

    public long generate_timeout = 10000;

    public boolean Disable_CozeBot_ReplyMsgCheck = false;
    public boolean Disable_Name_Cache = false;
    public boolean Disable_2000Limit_Unlock = false;

    /**
     * 额外标记 可填写任意内容
     */
    public String Mark = "";

}
