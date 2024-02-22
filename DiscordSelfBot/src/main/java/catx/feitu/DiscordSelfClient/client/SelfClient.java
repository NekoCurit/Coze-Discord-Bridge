package catx.feitu.DiscordSelfClient.client;

import catx.feitu.DiscordSelfClient.client.Exceptions.InvalidFileException;
import catx.feitu.DiscordSelfClient.client.Types.DiscordAttachment;
import catx.feitu.DiscordSelfClient.utils.DiscordAPIRequests;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import catx.feitu.DiscordSelfClient.client.impl.*;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SelfClient {

    private final DiscordAPIRequests requests;

    public SelfClient(String token) {
        requests = new DiscordAPIRequests(token);
    }
    public void setProxy(InetSocketAddress address) {
        requests.proxy = address;
    }
    public void setProxy(Proxy address) {
        requests.proxy = (InetSocketAddress) address.address();
    }
    public void setUserAgent(String agent) {
        requests.userAgent = agent;
    }
    public List<Friend> getFriends() throws Exception {
        List<Friend> friends = new ArrayList<>();

        JSONArray data = (JSONArray) requests.get("https://discord.com/api/v8/users/@me/relationships");
        for (int i = 0; i < data.size(); i++) {
            JSONObject object = data.getJSONObject(i);
            String id = object.getString("id");
            int type = object.getIntValue("type");
            String nickname = object.getString("nickname");
            JSONObject user = object.getJSONObject("user");
            String username = user.getString("username");
            String displayName = user.getString("display_name");
            String avatar = user.getString("avatar");
            String avatarDecoration = user.getString("avatar_decoration");
            String discriminator = user.getString("discriminator");
            int publicFlags = user.getIntValue("public_flags");
            Friend f = new Friend(id, type, nickname, username, displayName, avatar, avatarDecoration, discriminator, publicFlags);
            friends.add(f);
        }
        return friends;
    }

    public Token getSelf() throws Exception {
        JSONObject data = (JSONObject) requests.get("https://discord.com/api/v9/users/@me");
        String id = data.getString("id");
        String username = data.getString("username");
        String displayName = data.getString("display_name");
        String avatar = data.getString("avatar");
        String avatarDecoration = data.getString("avatar_decoration");
        String discriminator = data.getString("discriminator");
        int publicFlags = data.getIntValue("public_flags");
        int flags = data.getIntValue("flags");
        String banner = data.getString("banner");
        String bannerColor = data.getString("banner_color");
        int accentColor = data.getIntValue("accent_color");
        String bio = data.getString("bio");
        String locale = data.getString("locale");
        boolean nsfw = data.getBooleanValue("nsfw_allowed");
        boolean mfa = data.getBooleanValue("mfa_enabled");
        int premiumType = data.getIntValue("premium_type");
        String email = data.getString("email");
        boolean verified = data.getBooleanValue("verified");
        String phone = data.getString("phone");
        return new Token(id, username, displayName, avatar, avatarDecoration, discriminator, publicFlags, flags,
                banner, bannerColor, accentColor, bio, locale, nsfw, mfa, premiumType, email, verified, phone);
    }


    public List<Guild> getGuilds() throws Exception {
        List<Guild> guilds = new ArrayList<>();

        JSONArray data = (JSONArray) requests.get("<https://discord.com/api/v9/users/@me/guilds>");
        for (int i = 0; i < data.size(); i++) {
            JSONObject object = data.getJSONObject(i);
            String id = object.getString("id");
            String name = object.getString("name");
            String icon = object.getString("icon");
            boolean owner = object.getBooleanValue("owner");
            String permissions = object.getString("permissions");
            Guild guild = new Guild(id, name, icon, owner, permissions);
            guilds.add(guild);
        }
        return guilds;
    }

    public List<Channel> getChannels() throws Exception {
        List<Channel> channels = new ArrayList<>();

        JSONArray data = (JSONArray) requests.get("<https://discord.com/api/v8/users/@me/channels>");
        for (int i = 0; i < data.size(); i++) {
            JSONObject object = data.getJSONObject(i);
            String id = object.getString("id");
            int type = object.getIntValue("type");
            String lastMessageId = object.getString("last_message_id");
            int flags = object.getIntValue("flags");
            Channel channel = new Channel(id, type, lastMessageId, flags);
            channels.add(channel);
        }
        return channels;
    }

    public Channel getChannel(String channelId) throws Exception {
        JSONObject data = (JSONObject) requests.get("https://discord.com/api/v9/channels/" + channelId);

        String id = data.getString("id");
        Integer type = data.getInteger("type");
        String lastMessageId = data.getString("last_message_id");
        Integer flags = data.getInteger("flags");

        return new Channel(id, type, lastMessageId, flags);
    }

    public List<Message> getMessages(String channelId) throws Exception {
        List<Message> messages = new ArrayList<>();
        JSONArray data = (JSONArray) requests.get("https://discord.com/api/v9/channels/" + channelId + "/messages");
        for (int i = 0; i < data.size(); i++) {
            JSONObject object = data.getJSONObject(i);

            String id = object.getString("id");
            int type = object.getIntValue("type");
            String content = object.getString("content");

            messages.add(new Message(id, type, content));
        }
        return messages;
    }
     public void sendMessage(String message, String channelId, List<DiscordAttachment> attachments) throws Exception {
        JSONObject json = new JSONObject();
        json.put("content", message);

        if (!attachments.isEmpty()) {
            JSONArray json_attachments = new JSONArray();
            for (int i = 0; i < attachments.size(); i++) {
                DiscordAttachment attachment = attachments.get(i);

                if (attachment != null) {
                    JSONObject json_attachments_data = new JSONObject();

                    json_attachments_data.put("id", String.valueOf(i));
                    json_attachments_data.put("filename", attachment.fileName);
                    json_attachments_data.put("uploaded_filename", attachment.uploaded_filename);

                    json_attachments.add(json_attachments_data);
                }
            }

            json.put("channel_id", channelId);
            json.put("attachments", json_attachments);
        }
        requests.post("https://discord.com/api/v9/channels/" + channelId + "/messages", JSON.toJSONString(json));
    }
    public DiscordAttachment upLoadAttachment(byte[] fileByte, String fileName, String channelId) throws Exception {
        if (fileByte.length == 0) { throw new InvalidFileException(); }

        JSONObject json_files_data = new JSONObject();
        json_files_data.put("filename", fileName);
        json_files_data.put("file_size", fileByte.length);
        json_files_data.put("id", "1");
        json_files_data.put("is_clip", false);
        JSONArray json_files = new JSONArray();
        json_files.add(json_files_data);
        JSONObject json = new JSONObject();
        json.put("files", json_files);

        JSONObject jsonResponse = (JSONObject) requests.post("https://discord.com/api/v9/channels/" + channelId + "/attachments", JSON.toJSONString(json));
        URL fileURL = new URL(jsonResponse.getJSONArray("attachments").getJSONObject(0).getString("upload_url"));
        
        requests.put(fileURL.toString(), fileByte);
        
        return new DiscordAttachment(fileName, 1, fileURL.getPath().replaceFirst("/", ""));
    }
    public void leaveGuild(String guildId) throws Exception {
        requests.delete("https://discord.com/api/v9/users/@me/guilds/" + guildId);
    }

    public void joinGuild(String inviteCode) throws Exception {
        requests.post("https://discord.com/api/v9/invites/" + inviteCode, "{}");
    }

    public void deleteGuild(String guildId) throws Exception {
        requests.post("https://discord.com/api/v9/guilds/" + guildId + "/delete", "{}");
    }

    public void createGuild(String name, String category) throws Exception {
        JSONObject json = new JSONObject();
        json.put("name", name);
        if (category != null) json.put("parent_id", category);

        requests.post("<https://discord.com/api/v9/guilds>", JSON.toJSONString(json));
    }
    public void createGuild(String name) throws Exception {
        createGuild(name ,null);
    }

    public void addFriend(String name, String discriminator) throws Exception {
        JSONObject json = new JSONObject();
        json.put("username", name);
        json.put("discriminator", discriminator);
        requests.post("https://discord.com/api/v9/users/@me/relationships", JSON.toJSONString(json));
    }

    public void deleteFriend(String id) throws Exception {
        requests.delete("https://discord.com/api/v9/users/@me/relationships/" + id);
    }

    public void blockFriend(String id) throws Exception {
        JSONObject json = new JSONObject();
        json.put("type", 2);
        requests.put("https://discord.com/api/v9/users/@me/relationships/" + id, JSON.toJSONString(json));
    }

    public void changeAvatar(String avatar) throws Exception {
        JSONObject json = new JSONObject();
        json.put("avatar", avatar);

        requests.patch("https://discord.com/api/v9/users/@me", JSON.toJSONString(json));
    }

    public void clearAvatar() throws Exception {
        changeAvatar(null);
    }

    public void deleteMessages(String channelId, String messageId) throws Exception {
        requests.delete("https://discord.com/api/v9/channels/" + channelId + "/messages/" + messageId);
    }


    public void closeDms(String channelId) throws Exception {
        requests.delete("https://discord.com/api/v8/channels/" + channelId);
    }

    public void setStatus(String text) throws Exception {
        JSONObject custom_status = new JSONObject();
        custom_status.put("text", text);
        JSONObject json = new JSONObject();
        json.put("custom_status", custom_status);

        requests.patch("https://discord.com/api/v8/users/@me/settings", JSON.toJSONString(json));
    }
    public void changeBio(String text) throws Exception {
        JSONObject json = new JSONObject();
        json.put("bio", text);

        requests.patch("https://discord.com/api/v9/users/@me/profile", JSON.toJSONString(json));
    }

    public void changeTheme(Theme theme) throws Exception {
        JSONObject json = new JSONObject();
        switch (theme) {
            case DARK:
                json.put("theme", "dark");
                break;
            case LIGHT:
                json.put("theme", "light");
                break;
        }

        requests.patch("https://discord.com/api/v8/users/@me/settings", JSON.toJSONString(json));
    }


    public enum Theme {
        LIGHT, DARK;
    }
}
