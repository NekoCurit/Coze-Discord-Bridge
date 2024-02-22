package catx.feitu.CozeProxy.Protocol.Listener;

import catx.feitu.CozeProxy.Protocol.UniversalEventListener;
import catx.feitu.CozeProxy.Protocol.UniversalEventListenerConfig;
import catx.feitu.CozeProxy.Protocol.UniversalMessage;
import com.slack.api.app_backend.events.payload.EventsApiPayload;
import com.slack.api.bolt.handler.BoltEventHandler;
import com.slack.api.bolt.response.Response;
import com.slack.api.bolt.context.builtin.EventContext;
import com.slack.api.methods.SlackApiException;
import com.slack.api.model.event.MessageEvent;


import java.io.IOException;
import java.util.Objects;

public class SlackListener implements BoltEventHandler<MessageEvent> {
    public UniversalEventListener handle;
    public UniversalEventListenerConfig config;
    public SlackListener(UniversalEventListener handle, UniversalEventListenerConfig config) {
        this.handle = handle;
        this.config = config;
    }

    @Override
    public Response apply(EventsApiPayload<MessageEvent> event, EventContext context) throws IOException, SlackApiException {
        /*
        List<Attachment> attachments = event.getEvent().getAttachments();
        if (attachments != null && !attachments.isEmpty()) {
            for (Attachment attachment : attachments) {
                // 打印每个附件的信息, 示例中仅打印附件的回退文本
                System.out.println(attachment.get());
            }
        }
        */
        if (handle != null && Objects.equals(event.getEvent().getUser(), config.filterUserID)) {
            handle.onMessageStream(event.getEvent().getChannel(),
                    new UniversalMessage()
                            .setContent(event.getEvent().getText())
                            .setHasButton(true)
            );
        }
        return context.ack();
    }
}