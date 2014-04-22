package wunderrabbit;

import org.projectodd.wunderboss.messaging.Endpoint;

public class RabbitEndpoint implements Endpoint<String> {
    public RabbitEndpoint(String name, boolean broadcast, boolean durable) {
        this.name = name;
        this.broadcast = broadcast;
        this.durable = durable;
    }

    @Override
    public boolean isBroadcast() {
        return this.broadcast;
    }

    @Override
    public boolean isDurable() {
        return this.durable;
    }

    @Override
    public void close() throws Exception {

    }

    @Override
    public String implementation() {
        return this.name;
    }

    private final String name;
    private final boolean broadcast;
    private final boolean durable;
}
