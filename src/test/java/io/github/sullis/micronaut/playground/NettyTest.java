
package io.github.sullis.micronaut.playground;

import io.micronaut.http.server.netty.*;
import io.micronaut.http.server.netty.configuration.NettyHttpServerConfiguration;
import io.micronaut.http.server.netty.types.DefaultCustomizableResponseTypeHandlerRegistry;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Test;

@MicronautTest
public class NettyTest {

    @Test
    public void testNetty() {
        final var config = new NettyHttpServerConfiguration();
        final var services = new DefaultNettyEmbeddedServerFactory();
        final var handlerRegistry = new DefaultCustomizableResponseTypeHandlerRegistry();
        final var server = new NettyHttpServer(config, services, handlerRegistry, true);
    }
}