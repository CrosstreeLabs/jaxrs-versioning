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
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.DeploymentContext;
import org.glassfish.jersey.test.inmemory.InMemoryTestContainerFactory;
import org.glassfish.jersey.test.spi.TestContainer;
import org.glassfish.jersey.test.spi.TestContainerFactory;
import org.junit.rules.ExternalResource;

public class Jersey extends ExternalResource {
    
    private static final TestContainerFactory testContainerFactory = new InMemoryTestContainerFactory();
    private final Object[] objs;
    private DeploymentContext context;
    private TestContainer testContainer;
    private Client client;
    
    public Client getClient() {
        return client;
    }

    public Jersey(Object...objs) {
        this.objs = objs;
    }
    
    @Override
    protected void before() throws Throwable {
        ResourceConfig config = new ResourceConfig();
        for (Object obj : objs) {
            if (obj instanceof Class) {
                config.register((Class<?>)obj);
            } else {
                config.register(obj);
            }
        }
        
        context = DeploymentContext.builder(config).build();
        testContainer = testContainerFactory.create(URI.create("http://localhost:"+getPort()+"/"), context);
        testContainer.start();
        client = ClientBuilder.newClient(testContainer.getClientConfig());
        for (Object obj : objs) {
            if (obj instanceof Class) {
                client.register((Class<?>)obj);
            } else {
                client.register(obj);
            }
        }
    }

    @Override
    protected void after() {
        client.close();
        client = null;
        testContainer.stop();
        testContainer = null;
        context = null;
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