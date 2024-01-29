package catx.feitu.coze_discord_bridge.Misc;

import catx.feitu.coze_discord_bridge.Config.ConfigManage;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CacheManager {

    private static final Logger logger = LogManager.getLogger(CacheManager.class);

    public static JSONObject channel_name_cache = new JSONObject();
    public static JSONObject bot_reply_cache = new JSONObject();

    public static void LoadCache () {
        try {
            String content = Files.readString(Paths.get("cache_names.json"));
            JSON.parseObject(content);
        } catch (Exception e) {
            logger.warn("读取 cache_names.json 失败 (初次加载遇到此问题请忽略)",e);
        }
        SaveCache ();
    }
    public static void SaveCache () {
        try {
            Files.writeString(new File("cache_names.json").toPath(), channel_name_cache.toJSONString());
        } catch (Exception e) {
            logger.warn("保存 cache_names.json 失败",e);
        }
    }
    public static void Cache_AddName2Channel (String Name,String Channel) {
        if (!ConfigManage.Configs.Disable_Name_Cache) {
            channel_name_cache.put(Name,Channel);
            CacheManager.SaveCache();
        }
    }
    public static void Cache_DelName2Channel (String Name) {
        channel_name_cache.remove(Name);
        if (!ConfigManage.Configs.Disable_Name_Cache) {
            CacheManager.SaveCache();
        }
    }
    public static String Cache_GetName2Channel (String Name) {
        return ConfigManage.Configs.Disable_Name_Cache ? Name : (channel_name_cache.containsKey(Name) ? channel_name_cache.getString(Name) : Name);
    }

    public static void Cache_BotReplySave (String ChannelID, String prompt, List<String> files, Boolean Done) {
        bot_reply_cache.put("timestamp_" + ChannelID, Done ? 0 : Instant.now().toEpochMilli());
        bot_reply_cache.put("prompt_" + ChannelID, prompt);
        bot_reply_cache.put("files_" + ChannelID, files);
    }
    public static String Cache_BotReplyGetPrompt (String ChannelID) {
        if (!bot_reply_cache.containsKey("timestamp_" + ChannelID) || !bot_reply_cache.containsKey("prompt_" + ChannelID)) {
            return "";
        }
        long generate_time = bot_reply_cache.getLong("timestamp_" + ChannelID);
        if (generate_time == 0 || Instant.now().toEpochMilli() - generate_time > ConfigManage.Configs.generate_timeout) {
            return (bot_reply_cache.getString("prompt_" + ChannelID));
        }
        return "";
    }
    public static BotReplyType Cache_BotReplyMessageEx (String ChannelID) {
        BotReplyType Reply = new BotReplyType ();
        Reply.IsFound = bot_reply_cache.containsKey("timestamp_" + ChannelID);
        if (Reply.IsFound) {
            Reply.TextMessage = bot_reply_cache.getString("prompt_" + ChannelID);
            Reply.Files = bot_reply_cache.getObject("files_" + ChannelID, List.class);
            Reply.UpdataTimeStamp = bot_reply_cache.getLong("timestamp_" + ChannelID);
            Reply.Done = Reply.UpdataTimeStamp == 0 || Instant.now().toEpochMilli() - Reply.UpdataTimeStamp > ConfigManage.Configs.generate_timeout;
        }
        return Reply;
    }
    public static List<String> Cache_BotReplyGetFiles (String ChannelID) {
        if (!bot_reply_cache.containsKey("timestamp_" + ChannelID) || !bot_reply_cache.containsKey("prompt_" + ChannelID)) {
            return new ArrayList<>();
        }
        long generate_time = bot_reply_cache.getLong("timestamp_" + ChannelID);
        if (generate_time == 0 || Instant.now().toEpochMilli() - generate_time > ConfigManage.Configs.generate_timeout) {
            return bot_reply_cache.getObject("files_" + ChannelID, List.class);
        }
        return new ArrayList<>();
    }
    public static void Cache_BotReplyClear (String ChannelID) {
        bot_reply_cache.remove("prompt_" + ChannelID);
        bot_reply_cache.remove("timestamp_" + ChannelID);
    }

    public static String Cache_Default_OldMessage2Name (List<String> OldMessage) {
        if (!channel_name_cache.containsKey("OldMessage2Name")) {
            return "";
        }
        JSONArray messagesArray = channel_name_cache.getJSONArray("OldMessage2Name");
        logger.info(10);
        for (int i = 0; i < messagesArray.size(); i++) {
            JSONObject messageObject = messagesArray.getJSONObject(i);
            if (Objects.equals(messageObject.getString("message"), String.join(",", OldMessage))) {
                return (messageObject.getString("name"));
            }
        }
        return "";
    }
    public static void Cache_Default_NameWriteOldMessage (String Name,List<String> OldMessage) {
        JSONArray messagesArray = channel_name_cache.containsKey("OldMessage2Name") ?
                channel_name_cache.getJSONArray("OldMessage2Name"):
                new JSONArray();
        Boolean NeedCreate = true;
        for (int i = 0; i < messagesArray.size(); i++) {
            JSONObject messageObject = messagesArray.getJSONObject(i);
            if (Objects.equals(messageObject.getString("name"), Name)) {
                messageObject.put(String.join(",", OldMessage),OldMessage);
                messagesArray.set(i,messageObject);
                NeedCreate = false;
                break;
            }
        }
        if (NeedCreate) {
            JSONObject messageObject =new JSONObject();
            messageObject.put("name",Name);
            messageObject.put("message",String.join(",", OldMessage));
            messagesArray.add(messageObject);
        }
        channel_name_cache.put("OldMessage2Name",messagesArray);
    }
    public static void Cache_Default_NameDeleteOldMessage (String Name) {
        JSONArray messagesArray = channel_name_cache.getJSONArray("OldMessage2Name");
        for (int i = 0; i < messagesArray.size(); i++) {
            JSONObject messageObject = messagesArray.getJSONObject(i);
            if (Objects.equals(messageObject.getString("name"), Name)) {
                messagesArray.remove(i);
                break;
            }
        }
        channel_name_cache.put("OldMessage2Name",messagesArray);
    }
}
