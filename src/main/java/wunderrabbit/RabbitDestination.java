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
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.GetResponse;
import org.projectodd.wunderboss.Options;
import org.projectodd.wunderboss.codecs.Codec;
import org.projectodd.wunderboss.codecs.Codecs;
import org.projectodd.wunderboss.messaging.Destination;
import org.projectodd.wunderboss.messaging.Listener;
import org.projectodd.wunderboss.messaging.Message;
import org.projectodd.wunderboss.messaging.MessageHandler;

import java.util.Map;

public abstract class RabbitDestination implements Destination {
    public RabbitDestination(String name, RabbitConnection connection) {
        this.name = name;
        this.connection = connection;
    }

    public abstract boolean isDurable();

    @Override
    public String name() {
        return this.name;
    }

    @Override
    public Listener listen(final MessageHandler handler, final Codecs codecs,
                           final Map<ListenOption, Object> options) throws Exception {

        Channel channel = this.connection.internalConnection().createChannel();
        Consumer consumer = new DefaultConsumer(channel) {
            public void handleDelivery(final String _,
                                       final Envelope __,
                                       final BasicProperties properties,
                                       final byte[] body) {
                RabbitMessage message =
                        new RabbitMessage(body,
                                          properties,
                                          new DelegatingBytesCodec(codecs.forContentType(RabbitMessage.contentType(properties))),
                                          RabbitDestination.this);
                try {
                    handler.onMessage(message, RabbitDestination.this.connection);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };

        //TODO: concurrency
        this.connection.declareQueue(channel, this);
        String tag = channel.basicConsume(name(), consumer);

        return new RabbitListener(tag, channel);
    }

    @Override
    public void publish(Object content, Codec codec, Map<MessageOpOption, Object> options) throws Exception {
        Options<MessageOpOption> opts = new Options<>(options);
        codec = new DelegatingBytesCodec(codec);

        Channel channel = null;
        try {
            channel = this.connection.internalConnection().createChannel();
            this.connection.declareQueue(channel, this);
            BasicProperties props = new BasicProperties().builder()
                    .contentType(codec.contentType())
                    .headers((Map<String, Object>)opts.get(PublishOption.PROPERTIES))
                    .build();

            channel.basicPublish("", name(), props, (byte[])codec.encode(content));
        } finally {
            if (channel != null) {
                channel.close();
            }
        }

    }

    @Override
    public Message receive(Codecs codecs, Map<MessageOpOption, Object> options) throws Exception {
        Options<MessageOpOption> opts = new Options<>(options);

        Channel channel = null;
        RabbitMessage message = null;
        try {
            channel = this.connection.internalConnection().createChannel();
            this.connection.declareQueue(channel, this);
            //TODO: this auto-acks
            //TODO: what about timeouts?
            GetResponse response = channel.basicGet(name(), true);
            if (response != null) {
                BasicProperties props = response.getProps();
                message = new RabbitMessage(response.getBody(),
                                            props,
                                            new DelegatingBytesCodec(codecs.forContentType(RabbitMessage.contentType(props))),
                                            this);
            }
        } finally {
            if (channel != null) {
                channel.close();
            }
        }

        return message;
    }

    @Override
    public void stop() throws Exception {

    }

    private final String name;
    private final RabbitConnection connection;
}
