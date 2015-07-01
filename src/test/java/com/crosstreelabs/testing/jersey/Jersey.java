/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.crosstreelabs.testing.jersey;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.URI;
import java.util.concurrent.atomic.AtomicReference;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.DeploymentContext;
import org.glassfish.jersey.test.inmemory.InMemoryTestContainerFactory;
import org.glassfish.jersey.test.spi.TestContainer;
import org.glassfish.jersey.test.spi.TestContainerFactory;
import org.junit.rules.ExternalResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Jersey extends ExternalResource {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(Jersey.class);
    
    private final AtomicReference<Client> client = new AtomicReference<>();
    private static final TestContainerFactory testContainerFactory = new InMemoryTestContainerFactory();
    private final Class<?>[] classes;
    private DeploymentContext context;
    private TestContainer testContainer;
    
    public Client getClient() {
        return client.get();
    }

    public Jersey(Class<?>...classes) {
        this.classes = classes;
    }
    
    @Override
    protected void before() throws Throwable {
        context = DeploymentContext.builder(new ResourceConfig(classes)).build();
        testContainer = testContainerFactory.create(URI.create("http://localhost:"+getPort()+"/"), context);
        testContainer.start();
        Client client = ClientBuilder.newClient(testContainer.getClientConfig());
        for (Class<?> cls : classes) {
            client.register(cls);
        }
        this.client.set(client);
    }

    @Override
    protected void after() {
        client.get().close();
        client.set(null);
        testContainer.stop();
    }
    
    private int getPort() {
        try (ServerSocket ss = new ServerSocket(0)) {
            ss.setReuseAddress(true);
            return ss.getLocalPort();
        } catch (IOException ex) {
            throw new IllegalStateException(ex);
        }
    }
    
}