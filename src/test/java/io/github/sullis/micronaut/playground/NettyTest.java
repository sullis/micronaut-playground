
package io.github.sullis.micronaut.playground;

import io.micronaut.http.server.netty.*;
import io.micronaut.http.server.netty.configuration.NettyHttpServerConfiguration;
import io.micronaut.http.server.netty.types.DefaultCustomizableResponseTypeHandlerRegistry;
import org.junit.jupiter.api.Test;

public class NettyTest {

    @Test
    public void testNetty() {
        final var config = new NettyHttpServerConfiguration();
        final var services = new DefaultNettyEmbeddedServerFactory();
        final var handlerRegistry = new DefaultCustomizableResponseTypeHandlerRegistry();
        final var server = new NettyHttpServer(config, services, handlerRegistry, true);
    }
}