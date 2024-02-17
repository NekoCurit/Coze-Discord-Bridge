package catx.feitu.CozeProxy.Protocol.Listener;

import catx.feitu.CozeProxy.Protocol.UniversalEventListen;
import catx.feitu.CozeProxy.Protocol.UniversalMessage;
import com.slack.api.app_backend.events.payload.EventsApiPayload;
import com.slack.api.bolt.handler.BoltEventHandler;
import com.slack.api.bolt.response.Response;
import com.slack.api.bolt.context.builtin.EventContext;
import com.slack.api.methods.SlackApiException;
import com.slack.api.model.event.MessageEvent;


import java.io.IOException;

public class SlackListener implements BoltEventHandler<MessageEvent> {
    public UniversalEventListen handle;
    public SlackListener(UniversalEventListen handle) {
        this.handle = handle;
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
        handle.onMessageCreate(null ,event.getEvent().getChannel(),
                event.getEvent().getUser(),
                new UniversalMessage()
                        .setContent(event.getEvent().getText())
                        .setHasButton(true)
        );
        return context.ack();
    }
}