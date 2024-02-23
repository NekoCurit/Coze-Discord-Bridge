package catx.feitu.DiscordSelfClient.client;

import catx.feitu.DiscordSelfClient.client.Exceptions.CreateChannelException;
import catx.feitu.DiscordSelfClient.client.Exceptions.InvalidFileException;
import catx.feitu.DiscordSelfClient.client.Exceptions.InvalidMessageException;
import catx.feitu.DiscordSelfClient.client.Types.DiscordAttachment;
import catx.feitu.DiscordSelfClient.client.impl.*;
import catx.feitu.DiscordSelfClient.utils.DiscordAPIRequests;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

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
        if (address != null) {
            requests.proxy = (InetSocketAddress) address.address();
        } else {
            requests.proxy = (InetSocketAddress) null;
        }
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

        JSONArray data = (JSONArray) requests.get("https://discord.com/api/v8/users/@me/channels");
        System.out.print(data.toJSONString());
        for (int i = 0; i < data.size(); i++) {
            JSONObject object = data.getJSONObject(i);
            String id = object.getString("id");
            int type = object.getIntValue("type");
            String lastMessageId = object.getString("last_message_id");
            int flags = object.getIntValue("flags");
            Channel channel = new Channel(id, type, lastMessageId, flags, object.getString("name"));
            channels.add(channel);
        }
        return channels;
    }
    public Channel getChannel(String channelId) throws Exception {
        JSONObject data = (JSONObject) requests.get("https://discord.com/api/v9/channels/" + channelId);

        return new Channel(data.getString("id"),
                data.getInteger("type"),
                data.getString("last_message_id"),
                data.getInteger("flags"),
                data.getString("name")
        );
    }

    public List<Message> getMessages(String channelId) throws Exception {
        List<Message> messages = new ArrayList<>();
        JSONArray data = (JSONArray) requests.get("https://discord.com/api/v9/channels/" + channelId + "/messages");

        for (int i = 0; i < data.size(); i++) {
            JSONObject object = data.getJSONObject(i);
            String id = object.getString("id");
            List<Attachment> attachments = new ArrayList<>();
            List<User> mentions = new ArrayList<>();

            JSONObject object_author = object.getJSONObject("author");
            User user = new User(object_author.getString("id") ,object_author.getString("username") ,object_author.getBooleanValue("bot"));

            JSONArray object_attachments = object.getJSONArray("attachments");
            for(int j = 0; j < object_attachments.size(); j++) {
                JSONObject object_attachments_object = object_attachments.getJSONObject(j);
                attachments.add(new Attachment(
                        object_attachments_object.getString("id"),
                        object_attachments_object.getString("filename"),
                        object_attachments_object.getString("proxy_url"),
                        object_attachments_object.getString("url")
                ));
            }
            JSONArray object_mentions = object.getJSONArray("mentions");
            for(int j = 0; j < object_attachments.size(); j++) {
                JSONObject object_mentions_object = object_attachments.getJSONObject(j);
                mentions.add(new User(
                        object_mentions_object.getString("id"),
                        object_mentions_object.getString("username"),
                        false
                ));
            }
            messages.add(new Message(id,
                    object.getIntValue("type"),
                    object.getString("content"),
                    user, attachments ,mentions ,!object.getJSONArray("components").isEmpty()));
        }
        return messages;
    }
    public Message getLatestMessage(String channelId) throws Exception {
        return getMessages(channelId).get(0);
    }
    public Message getMessage(String channelId ,String messageId) throws Exception {
        JSONArray data = (JSONArray) requests.get("https://discord.com/api/v9/channels/" + channelId + "/messages");
        for (int i = 0; i < data.size(); i++) {
            JSONObject object = data.getJSONObject(i);

            String id = object.getString("id");
            if (!Objects.equals(id, messageId)) continue;
            List<Attachment> attachments = new ArrayList<>();
            List<User> mentions = new ArrayList<>();

            JSONObject object_author = object.getJSONObject("author");
            User user = new User(object_author.getString("id") ,object_author.getString("username") ,object_author.getBoolean("bot"));

            JSONArray object_attachments = object.getJSONArray("attachments");
            for(int j = 0; j < object_attachments.size(); j++) {
                JSONObject object_attachments_object = object_attachments.getJSONObject(j);
                attachments.add(new Attachment(
                        object_attachments_object.getString("id"),
                        object_attachments_object.getString("filename"),
                        object_attachments_object.getString("proxy_url"),
                        object_attachments_object.getString("url")
                ));
            }
            JSONArray object_mentions = object.getJSONArray("mentions");
            for(int j = 0; j < object_attachments.size(); j++) {
                JSONObject object_mentions_object = object_attachments.getJSONObject(j);
                mentions.add(new User(
                        object_mentions_object.getString("id"),
                        object_mentions_object.getString("username"),
                        false
                ));
            }
            return new Message(id,
                    object.getIntValue("type"),
                    object.getString("content"),
                    user, attachments ,mentions ,!object.getJSONArray("components").isEmpty());
        }
        throw new InvalidMessageException();
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

    public void createGuild(String name) throws Exception {
        JSONObject json = new JSONObject();
        json.put("name", name);

        requests.post("https://discord.com/api/v9/guilds", JSON.toJSONString(json));
    }
    public String createChannel(String guild, String name, String category) throws Exception {
        JSONObject json = new JSONObject(true);
        json.put("type", 0);
        json.put("name", name);
        json.put("permission_overwrites", new JSONArray());
        if (category != null && !category.isEmpty()) json.put("parent_id", category);
        JSONObject object = (JSONObject) requests.post("https://discord.com/api/v9/guilds/" + guild + "/channels", JSON.toJSONString(json));
        if (!object.containsKey("id")) { throw new CreateChannelException(object.toJSONString()); }
        return (object.getString("id"));
    }
    public void deleteChannel(String channelId) throws Exception {
        requests.delete("https://discord.com/api/v9/channels/" + channelId);
    }
    public void renameChannel(String channelId,String name) throws Exception {
        JSONObject json = new JSONObject();
        json.put("name", name);
        json.put("type", 0);
        json.put("topic", name);
        json.put("bitrate", 64000);
        json.put("user_limit", 0);
        json.put("nsfw", false);
        json.put("flags", 0);
        json.put("rate_limit_per_user", 0);

        requests.patch("https://discord.com/api/v9/channels/" + channelId, json.toJSONString());
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
