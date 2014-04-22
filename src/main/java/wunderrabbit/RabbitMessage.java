package wunderrabbit;

import com.rabbitmq.client.AMQP;
import org.projectodd.wunderboss.messaging.Endpoint;
import org.projectodd.wunderboss.messaging.Message;
import org.projectodd.wunderboss.messaging.Response;

import java.util.Map;

public class RabbitMessage implements Message<Void> {
    public RabbitMessage(byte[] body, AMQP.BasicProperties properties, Endpoint<String> endpoint) {
        this.body = body;
        this.properties = properties;
        this.endpoint = endpoint;
    }
    @Override
    public String contentType() {
        return this.properties.getContentType();
    }

    @Override
    public Map<String, Object> headers() {
        return this.properties.getHeaders();
    }

    @Override
    public Endpoint endpoint() {
        return this.endpoint;
    }

    @Override
    public boolean acknowledge() throws Exception {
        return false;
    }

    @Override
    public Response reply(byte[] content, String contentType, Map options) throws Exception {
        return null;
    }

    @Override
    public Response reply(String content, String contentType, Map options) throws Exception {
        return null;
    }

    @Override
    public <T> T body(Class T) {
        if (T == String.class) {
            return (T)new String(this.body);
        } else {
            return (T)body;
        }
    }

    @Override
    public Void implementation() {
        return null;
    }

    private final AMQP.BasicProperties properties;
    private final byte[] body;
    private final Endpoint endpoint;

}
