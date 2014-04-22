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
import org.projectodd.wunderboss.Options;
import org.projectodd.wunderboss.messaging.Endpoint;
import org.projectodd.wunderboss.messaging.Messaging;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RabbitMessaging implements Messaging<ConnectionFactory, String, Connection> {

    public RabbitMessaging(String name, Options<CreateOption> options) {
        this.name = name;
        this.options = options;
    }

    @Override
    public Endpoint findOrCreateEndpoint(String name, Map<CreateEndpointOption, Object> options) throws Exception {
        Options<CreateEndpointOption> opts = new Options<>(options);

        return new RabbitEndpoint(name,
                                  opts.getBoolean(CreateEndpointOption.BROADCAST, false),
                                  opts.getBoolean(CreateEndpointOption.DURABLE,
                                                  (Boolean)CreateEndpointOption.DURABLE.defaultValue));
    }

    @Override
    public org.projectodd.wunderboss.messaging.Connection createConnection(Map options) throws Exception {
        Connection conn = this.connectionFactory.newConnection();
        connections.add(conn);

        return new RabbitConnection(conn);
    }

    @Override
    public boolean isXaDefault() {
        return false;
    }

    @Override
    public void start() throws Exception {
        if (!this.started) {
            this.connectionFactory = new ConnectionFactory();
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
    public String name() {
        return this.name;
    }

    @Override
    public ConnectionFactory implementation() {
        return this.connectionFactory;
    }

    private final String name;
    private final Options<CreateOption> options;
    private boolean started = false;
    private ConnectionFactory connectionFactory;
    private final List<Connection> connections = new ArrayList<>();
}
