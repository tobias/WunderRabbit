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
