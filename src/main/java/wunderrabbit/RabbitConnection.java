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


import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.GetResponse;
import org.projectodd.wunderboss.Options;
import org.projectodd.wunderboss.messaging.Endpoint;
import org.projectodd.wunderboss.messaging.Listener;
import org.projectodd.wunderboss.messaging.Message;
import org.projectodd.wunderboss.messaging.MessageHandler;
import org.projectodd.wunderboss.messaging.Response;

import java.util.Map;

public class RabbitConnection implements org.projectodd.wunderboss.messaging.Connection<Connection> {

    public RabbitConnection(Connection connection) {
        this.connection = connection;
    }

    @Override
    public Listener listen(final Endpoint endpoint, final MessageHandler handler,
                           Map<ListenOption, Object> options) throws Exception {

        Channel channel = this.connection.createChannel();
        Consumer consumer = new DefaultConsumer(channel) {
            public void handleDelivery(String tag, Envelope envelope, BasicProperties properties, byte[] body) {
                RabbitMessage message = new RabbitMessage(body, properties, endpoint);
                try {
                    handler.onMessage(message);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };

        //TODO: concurrency
        declareQueue(channel, endpoint);
        String tag = channel.basicConsume((String)endpoint.implementation(), consumer);

        return new RabbitListener(tag, channel);
    }

    @Override
    public Listener respond(Endpoint endpoint, MessageHandler handler,
                            Map<ListenOption, Object> options) throws Exception {
        return null;
    }

    @Override
    public void send(Endpoint endpoint, String content, String contentType,
                     Map<SendOption, Object> options) throws Exception {
        send(endpoint, content.getBytes(), contentType, options);
    }

    @Override
    public void send(Endpoint endpoint, byte[] content, String contentType,
                     Map<SendOption, Object> options) throws Exception {
        Options<SendOption> opts = new Options<>(options);

        Channel channel = null;
        try {
            channel = this.connection.createChannel();
            declareQueue(channel, endpoint);
            BasicProperties props = new BasicProperties().builder()
                    .contentType(contentType)
                    .headers((Map<String, Object>)opts.get(SendOption.HEADERS))
                    .build();

            channel.basicPublish("", (String)endpoint.implementation(), props, content);
        } finally {
            if (channel != null) {
                channel.close();
            }
        }

    }

    @Override
    public Response request(Endpoint endpoint, String content, String contentType,
                            Map<SendOption, Object> options) throws Exception {
        return null;
    }

    @Override
    public Response request(Endpoint endpoint, byte[] content, String contentType,
                            Map<SendOption, Object> options) throws Exception {
        return null;
    }

    @Override
    public Message receive(Endpoint endpoint, Map<ReceiveOption, Object> options) throws Exception {
        Options<ReceiveOption> opts = new Options<>(options);

        Channel channel = null;
        RabbitMessage message = null;
        try {
            channel = this.connection.createChannel();
            declareQueue(channel, endpoint);
            //TODO: this auto-acks
            //TODO: what about timeouts?
            GetResponse response = channel.basicGet((String)endpoint.implementation(), true);
            if (response != null) {
                message = new RabbitMessage(response.getBody(), response.getProps(), endpoint);
            }
        } finally {
            if (channel != null) {
                channel.close();
            }
        }

        return message;
    }

    @Override
    public void close() throws Exception {
        this.connection.close();
    }

    @Override
    public Connection implementation() {
        return this.connection;
    }

    //TODO: handle broadcast (topic) semantics
    protected void declareQueue(Channel channel, Endpoint<String> endpoint) throws Exception {
        channel.queueDeclare(endpoint.implementation(), endpoint.isDurable(), false, false, null);
    }

    private final Connection connection;
}
