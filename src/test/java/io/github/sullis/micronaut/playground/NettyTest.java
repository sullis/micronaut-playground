
package io.github.sullis.micronaut.playground;

import io.micronaut.http.server.netty.*;
import io.micronaut.http.server.netty.configuration.NettyHttpServerConfiguration;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

@MicronautTest
public class NettyTest {
    @Inject
    DefaultNettyEmbeddedServerFactory nettyEmbeddedServerFactory;


    @Test
    public void testNetty() {
        final var config = new NettyHttpServerConfiguration();
        final var server = new NettyHttpServer(config, nettyEmbeddedServerFactory, true);
        server.start();
        server.stop();
        server.close();
    }
}