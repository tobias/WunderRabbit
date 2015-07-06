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


import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import org.projectodd.wunderboss.messaging.Context;

public class RabbitConnection implements Context {

    public RabbitConnection(Connection connection) {
        this.connection = connection;
    }

    @Override
    public Mode mode() {
        return null;
    }

    @Override
    public void commit() {

    }

    @Override
    public void rollback() {

    }

    @Override
    public void acknowledge() {

    }

    @Override
    public boolean enlist() throws Exception {
        return false;
    }

    @Override
    public void addCloseable(AutoCloseable closeable) {

    }

    @Override
    public boolean isRemote() {
        return true;
    }

    @Override
    public void close() throws Exception {
        this.connection.close();
    }

    public Connection internalConnection() {
        return this.connection;
    }

    //TODO: handle broadcast (topic) semantics
    protected void declareQueue(Channel channel, RabbitDestination dest) throws Exception {
        channel.queueDeclare(dest.name(), dest.isDurable(), false, false, null);
    }

    private final Connection connection;


}
