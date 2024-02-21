package catx.feitu.DiscordSelfClient.utils;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;

public class Jsons {

    public String getString(JsonElement jsonElement) {
        return jsonElement == JsonNull.INSTANCE ? null : jsonElement.getAsString();
    }

    public int getInt(JsonElement jsonElement) {
        return jsonElement == JsonNull.INSTANCE ? -1 : jsonElement.getAsInt();
    }

    public long getLong(JsonElement jsonElement) {
        return jsonElement == JsonNull.INSTANCE ? -1 : jsonElement.getAsLong();
    }


    public boolean getBoolean(JsonElement jsonElement) {
        return jsonElement == JsonNull.INSTANCE ? null : jsonElement.getAsBoolean();
    }
}