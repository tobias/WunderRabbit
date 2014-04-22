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
