package wunderrabbit;

import org.projectodd.wunderboss.ComponentProvider;
import org.projectodd.wunderboss.Options;

public class RabbitMessagingProvider implements ComponentProvider<RabbitMessaging> {
    @Override
    public RabbitMessaging create(String name, Options options) {
        return new RabbitMessaging(name, options);
    }
}
