package catx.feitu.CozeProxy.utils;

import catx.feitu.CozeProxy.utils.extensions.Protocol;

import java.util.ArrayList;
import java.util.List;

public class ProtocolUtils {
    public static List<Protocol> getAliveProtocols (List<Protocol> protocols) {
        List<Protocol> aliveProtocols = new ArrayList<>();
        for (Protocol protocol : protocols) {
            if (protocol.limitedDate == null || DateUtils.isBeforeNineAMNextDay(protocol.limitedDate,9)) {
                aliveProtocols.add(protocol);
            }
        }
        return aliveProtocols;
    }
}
