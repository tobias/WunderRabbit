package wunderrabbit;

import org.projectodd.wunderboss.codecs.Codec;
import org.projectodd.wunderboss.codecs.Codecs;
import org.projectodd.wunderboss.messaging.Listener;
import org.projectodd.wunderboss.messaging.MessageHandler;
import org.projectodd.wunderboss.messaging.Queue;
import org.projectodd.wunderboss.messaging.Response;

import java.util.Map;

public class RabbitQueue extends RabbitDestination implements Queue {

    public RabbitQueue(String name, boolean durable, RabbitConnection connection) {
        super(name, connection);
        this.durable = durable;
    }

    @Override
    public Listener respond(MessageHandler handler, Codecs codecs, Map<ListenOption, Object> options) throws Exception {
        return null;
    }

    @Override
    public Response request(Object content, Codec codec, Codecs codecs, Map<MessageOpOption, Object> options) throws Exception {
        return null;
    }

    public boolean isDurable() {
        return this.durable;
    }

    private final boolean durable;
}
