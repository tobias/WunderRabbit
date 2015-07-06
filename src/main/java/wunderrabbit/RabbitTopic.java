package wunderrabbit;

import org.projectodd.wunderboss.codecs.Codecs;
import org.projectodd.wunderboss.messaging.Listener;
import org.projectodd.wunderboss.messaging.MessageHandler;
import org.projectodd.wunderboss.messaging.Topic;

import java.util.Map;

public class RabbitTopic extends RabbitDestination implements Topic {

    public RabbitTopic(String name, RabbitConnection connection) {
        super(name, connection);
    }

    @Override
    public boolean isDurable() {
        return false;
    }

    @Override
    public Listener subscribe(String id, MessageHandler handler, Codecs codecs, Map<SubscribeOption, Object> options) throws Exception {
        return null;
    }

    @Override
    public void unsubscribe(String id, Map<UnsubscribeOption, Object> options) throws Exception {

    }
}
