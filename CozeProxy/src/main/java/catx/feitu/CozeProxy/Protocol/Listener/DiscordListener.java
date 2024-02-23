package catx.feitu.CozeProxy.Protocol.Listener;

import catx.feitu.CozeProxy.Protocol.UniversalEventListener;
import catx.feitu.CozeProxy.Protocol.UniversalEventListenerConfig;


public class DiscordListener {
    public UniversalEventListener handle;
    public UniversalEventListenerConfig config;
    public DiscordListener(UniversalEventListener handle, UniversalEventListenerConfig config) {
        this.handle = handle;
        this.config = config;
    }
}