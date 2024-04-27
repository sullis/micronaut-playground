
package io.github.sullis.micronaut.playground;

import io.micronaut.http.HttpVersion;
import io.micronaut.http.server.netty.*;
import io.micronaut.http.server.netty.configuration.NettyHttpServerConfiguration;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.netty.handler.codec.compression.Brotli;
import io.netty.handler.codec.compression.Zstd;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.ssl.OpenSsl;
import io.netty.incubator.channel.uring.IOUring;
import jakarta.inject.Inject;
import org.apache.commons.lang3.SystemUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

@MicronautTest
public class NettyTest {
    @Inject
    DefaultNettyEmbeddedServerFactory nettyEmbeddedServerFactory;

    @BeforeAll
    static void beforeAllTests() throws Throwable {
        OpenSsl.ensureAvailability();
        assertThat(OpenSsl.versionString()).isEqualTo("BoringSSL");
        Zstd.ensureAvailability();
        Brotli.ensureAvailability();
        if (SystemUtils.IS_OS_LINUX) {
            IOUring.ensureAvailability();
        }
    }

    @Test
    public void testNetty() throws Exception {
        final var config = new NettyHttpServerConfiguration();
        config.setLogLevel(LogLevel.INFO);
        config.setUseNativeTransport(true);
        config.setDefaultCharset(StandardCharsets.UTF_8);
        config.setHttpVersion(HttpVersion.HTTP_2_0);

        final var server = nettyEmbeddedServerFactory.build(config);

        server.start();
        assertThat(server.isRunning()).isTrue();

        final int port = server.getPort();

        HttpClient client = HttpClient.newBuilder().build();
        HttpRequest request = HttpRequest.newBuilder().GET()
                .uri(URI.create("http://localhost:" + port + "/"))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        // todo assertThat(response.statusCode()).isEqualTo(200);

        server.stop();
        assertThat(server.isRunning()).isFalse();
        server.close();
    }
}