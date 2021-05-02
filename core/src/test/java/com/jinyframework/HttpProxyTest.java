package com.jinyframework;

import com.jinyframework.core.AbstractRequestBinder.HttpResponse;
import lombok.val;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("api.HttpProxyTest")
public class HttpProxyTest {
    static final String url = "http://localhost:8000";

    private final boolean isCI =
            System.getenv("CI") != null
                    && System.getenv("CI").equalsIgnoreCase("true");

    @BeforeAll
    static void startProxy() throws InterruptedException {
        val serverBIO = HttpServer.port(1111);
        serverBIO.get("/hello", ctx -> HttpResponse.of("Hello World"));
        new Thread(serverBIO::start).start();

        val serverNIO = NIOHttpServer.port(2222);
        serverNIO.get("/bye", ctx -> HttpResponse.ofAsync("Bye"));
        serverNIO.post("/echo", ctx -> HttpResponse.ofAsync(ctx.getBody()));
        new Thread(serverNIO::start).start();

        val proxy = HttpProxy.port(8000);
        proxy.use("/bio", "localhost:1111");
        proxy.use("/nio", "localhost:2222");
        new Thread(proxy::start).start();

        // Wait for all servers  to start
        TimeUnit.SECONDS.sleep(2);
    }

    @Test
    @DisplayName("Hello World")
    void helloWorld() throws IOException {
        val res = HttpClient.builder()
                .url(url + "/bio/hello").method("GET")
                .build().perform();
        assertEquals("Hello World", res.getBody());
    }

    @Test
    @DisplayName("Bye")
    void bye() throws IOException {
        val res = HttpClient.builder()
                .url(url + "/nio/bye").method("GET")
                .build().perform();
        assertEquals(res.getBody(), "Bye");
    }

    @Test
    @DisplayName("Default")
    void defaultHandler() throws IOException {
        val res = HttpClient.builder()
                .url(url + "/").method("GET")
                .build().perform();
        assertEquals(404, res.getStatus());
    }

    @Test
    @DisplayName("Not Found")
    void notFound() throws IOException {
        val res = HttpClient.builder()
                .url(url + "/404").method("GET")
                .build().perform();
        assertEquals(404, res.getStatus());
    }

    @Test
    @DisplayName("Echo")
    void echo() throws IOException {
        if (isCI) return;

        val res = HttpClient.builder()
                .url(url + "/nio/echo").method("POST")
                .body("Hello World!")
                .build().perform();
        assertEquals("Hello World!", res.getBody());
    }

    @AfterEach
    void each() throws InterruptedException {
        if (isCI) {
            TimeUnit.MILLISECONDS.sleep(100);
        }
    }
}
