
package io.github.sullis.micronaut.playground;

import io.micronaut.http.HttpVersion;
import io.micronaut.http.server.netty.*;
import io.micronaut.http.server.netty.configuration.NettyHttpServerConfiguration;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.netty.handler.logging.LogLevel;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

@MicronautTest
public class NettyTest {
    @Inject
    DefaultNettyEmbeddedServerFactory nettyEmbeddedServerFactory;


    @Test
    public void testNetty() {
        final var config = new NettyHttpServerConfiguration();
        config.setLogLevel(LogLevel.INFO);
        config.setUseNativeTransport(true);
        config.setDefaultCharset(StandardCharsets.UTF_8);
        config.setHttpVersion(HttpVersion.HTTP_2_0);

        final var server = nettyEmbeddedServerFactory.build(config);

        server.start();
        assertThat(server.isRunning()).isTrue();
        server.stop();
        assertThat(server.isRunning()).isFalse();
        server.close();
    }
}