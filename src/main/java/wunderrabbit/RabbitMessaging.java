/*
 * Copyright 2014 Red Hat, Inc, and individual contributors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package wunderrabbit;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.projectodd.wunderboss.Option;
import org.projectodd.wunderboss.Options;
import org.projectodd.wunderboss.messaging.Context;
import org.projectodd.wunderboss.messaging.Messaging;
import org.projectodd.wunderboss.messaging.Queue;
import org.projectodd.wunderboss.messaging.Topic;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RabbitMessaging implements Messaging {

    public RabbitMessaging(String name, Options<CreateOption> options) {
        this.name = name;
        this.options = options;
    }

    @Override
    public Queue findOrCreateQueue(String name, Map<CreateQueueOption, Object> options) throws Exception {
        Options<CreateQueueOption> opts = new Options<>(options);

        RabbitConnection ctx = ensureContext(opts, CreateQueueOption.CONTEXT);

        return new RabbitQueue(name,
                               opts.getBoolean(CreateQueueOption.DURABLE),
                               ctx);
    }

    @Override
    public Topic findOrCreateTopic(String name, Map<CreateTopicOption, Object> options) throws Exception {
        Options<CreateTopicOption> opts = new Options<>(options);

        RabbitConnection ctx = ensureContext(opts, CreateTopicOption.CONTEXT);

        return new RabbitTopic(name, ctx);
    }

    @Override
    public Context createContext(Map options) throws Exception {
        Connection conn = this.connectionFactory.newConnection();
        connections.add(conn);

        return new RabbitConnection(conn);
    }


    @Override
    public void start() throws Exception {
        if (!this.started) {
            this.connectionFactory = new ConnectionFactory();
            //TODO: deal with port, using the rabbit default
            this.connectionFactory.setHost(this.options.getString(CreateOption.HOST, "localhost"));
            this.started = true;
        }

    }

    @Override
    public void stop() throws Exception {
        if (this.started) {
            this.connectionFactory = null;
            for(Connection each : connections) {
                each.close();
            }
            this.connections.clear();
            this.started = false;
        }
    }

    @Override
    public boolean isRunning() {
        return this.started;
    }

    public RabbitConnection ensureContext(Options opts, Option key) {
        RabbitConnection ctx = (RabbitConnection)opts.get(key);
        if (ctx == null) {
            throw new IllegalArgumentException("A context is required.");
        }

        return ctx;
    }


    public ConnectionFactory rabbitConnectionFactory() {
        return this.connectionFactory;
    }

    @Override
    public String name() {
        return this.name;
    }

    private final String name;
    private final Options<CreateOption> options;
    private boolean started = false;
    private ConnectionFactory connectionFactory;
    private final List<Connection> connections = new ArrayList<>();
}
