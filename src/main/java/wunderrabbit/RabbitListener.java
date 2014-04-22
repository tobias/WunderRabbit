package wunderrabbit;

import com.rabbitmq.client.Channel;
import org.projectodd.wunderboss.messaging.Listener;

public class RabbitListener implements Listener {

    public RabbitListener(String tag, Channel channel) {
        this.tag = tag;
        this.channel = channel;
    }

    @Override
    public void close() throws Exception {
        channel.basicCancel(this.tag);
        channel.close();
    }

    private final String tag;
    private final Channel channel;
}
