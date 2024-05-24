
package io.github.sullis.micronaut.playground;

import io.micronaut.http.HttpVersion;
import io.micronaut.http.netty.channel.EpollAvailabilityCondition;
import io.micronaut.http.netty.channel.IoUringAvailabilityCondition;
import io.micronaut.http.netty.channel.KQueueAvailabilityCondition;
import io.micronaut.http.server.netty.*;
import io.micronaut.http.server.netty.configuration.NettyHttpServerConfiguration;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.kqueue.KQueue;
import io.netty.handler.codec.compression.Brotli;
import io.netty.handler.codec.compression.Zstd;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.ssl.OpenSsl;
import io.netty.incubator.channel.uring.IOUring;
import jakarta.inject.Inject;
import org.apache.commons.compress.compressors.CompressorInputStream;
import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.SystemUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;


@MicronautTest
public class NettyTest {
    private static final Charset CHARSET = StandardCharsets.UTF_8;

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
            Epoll.ensureAvailability();
        }
        if (SystemUtils.IS_OS_MAC_OSX) {
            KQueue.ensureAvailability();
        }
    }

    public static Stream<Arguments> compressionParams() {
        return Stream.of(
                Arguments.of("gzip", "gzip"),
                Arguments.of("br", "br"),
                Arguments.of("zstd", "zstd"),
                Arguments.of("gzip, br", "br"),
                Arguments.of("gzip, zstd", "zstd"),
                Arguments.of("identity", null),
                Arguments.of("", null),
                Arguments.of(",", null),
                Arguments.of("bogus", null)
        );
    }

    @ParameterizedTest
    @MethodSource("compressionParams")
    public void testCompression(String acceptEncoding, String expectedContentEncoding) throws Exception {
        final var config = new NettyHttpServerConfiguration();
        config.setLogLevel(LogLevel.INFO);
        config.setUseNativeTransport(true);
        config.setDefaultCharset(CHARSET);
        config.setHttpVersion(HttpVersion.HTTP_2_0);

        final var server = nettyEmbeddedServerFactory.build(config);

        server.start();
        assertThat(server.isRunning()).isTrue();

        HttpCompressionStrategy strategy = server.getApplicationContext().findBean(HttpCompressionStrategy.class).get();
        assertThat(strategy.isEnabled()).isTrue();

        final URI uri = URI.create(server.getScheme() + "://localhost:" + server.getPort() + "/");

        HttpClient client = HttpClient.newBuilder().build();
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder().GET().uri(uri);
        if (acceptEncoding != null) {
            requestBuilder = requestBuilder.header("Accept-Encoding", acceptEncoding);
        }
        HttpRequest request = requestBuilder.build();
        HttpResponse<InputStream> response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());
        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.headers().firstValue("Content-Type")).isNotEmpty();
        String contentEncoding = response.headers().firstValue("Content-Encoding").orElse(null);
        assertThat(contentEncoding).isEqualTo(expectedContentEncoding);
        if (null == expectedContentEncoding) {
            String actualBody = IOUtils.toString(response.body(), CHARSET);
            assertThat(actualBody).isEqualTo(Constants.PAYLOAD);
        } else {
            String decompressedBody = decompress(response.body(), expectedContentEncoding);
            assertThat(decompressedBody).isEqualTo(Constants.PAYLOAD);
        }

        server.stop();
        assertThat(server.isRunning()).isFalse();
        server.close();
    }

    @Test
    @EnabledOnOs(value = OS.LINUX)
    void epollIsAvailableOnLinux() {
        var condition = new EpollAvailabilityCondition();
        assertTrue(condition.matches(null));
    }

    @Test
    @EnabledOnOs(value = OS.MAC)
    void kqueueIsAvailableOnMac() {
        var condition = new KQueueAvailabilityCondition();
        assertTrue(condition.matches(null));
    }

    @Test
    @EnabledOnOs(value = OS.LINUX)
    void ioUringIsAvailableOnLinux() {
        var condition = new IoUringAvailabilityCondition();
        assertTrue(condition.matches(null));
    }

    private static String decompress(final InputStream compressedData, final String contentEncoding) throws Exception {
        CompressorInputStream decompressor = new CompressorStreamFactory().createCompressorInputStream(toCommonsCompressName(contentEncoding), compressedData);
        return IOUtils.toString(decompressor, CHARSET);
    }

    private static String toCommonsCompressName(String contentEncoding) {
        if (contentEncoding == null) {
            return null;
        }
        else if (contentEncoding.equals("gzip")) {
            return "gz";
        } else {
            return contentEncoding;
        }
    }
}