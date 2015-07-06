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

import com.rabbitmq.client.AMQP;
import org.projectodd.wunderboss.codecs.Codec;
import org.projectodd.wunderboss.messaging.Destination;
import org.projectodd.wunderboss.messaging.ReplyableMessage;

import java.util.Map;

public class RabbitMessage implements ReplyableMessage {
    public RabbitMessage(byte[] body, AMQP.BasicProperties properties, Codec codec, Destination destination) {
        this.body = body;
        this.properties = properties;
        this.codec = codec;
        this.destination = destination;
    }

    @Override
    public String id() {
        return this.properties.getMessageId();
    }

    @Override
    public String contentType() {
        return contentType(this.properties);
    }

    public static String contentType(AMQP.BasicProperties properties) {
        return properties.getContentType();
    }

    @Override
    public Map<String, Object> properties() {
        return this.properties.getHeaders();
    }

    @Override
    public Destination endpoint() {
        return this.destination;
    }

    @Override
    public boolean acknowledge() throws Exception {
        return false;
    }

    @Override
    public void reply(Object content, Codec codec, Map<Destination.MessageOpOption, Object> options) throws Exception {

    }

    @Override
    public Object body() {
        return this.codec.decode(this.body);
    }

    private final AMQP.BasicProperties properties;
    private final byte[] body;
    private final Codec codec;
    private final Destination destination;

}
