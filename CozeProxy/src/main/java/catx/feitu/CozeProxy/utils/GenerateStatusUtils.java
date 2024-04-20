package catx.feitu.CozeProxy.utils;

import catx.feitu.CozeProxy.data.datas.GenerateStatusData;

public class GenerateStatusUtils {
    private final GenerateStatusData data;
    public GenerateStatusUtils(GenerateStatusData _data) {
        data = _data;
    }

    public void saveGenerateStatus(String channelID) {
        data.startGenerates.put(channelID, true);
    }

    public void clearGenerateStatus(String channelID) {
        data.startGenerates.remove(channelID);
    }

    public boolean getGenerateStatus(String channelID) {
        return data.startGenerates.getOrDefault(channelID, false);
    }
}