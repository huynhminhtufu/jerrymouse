package com.jinyframework;

import com.jinyframework.core.AbstractRequestBinder.HttpResponse;
import lombok.val;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class LoopTest {
    private final String url;
    private static final HttpServer server = HttpServer.port(1234);

    public LoopTest() {
        this.url = "http://localhost:1234";
    }

    @BeforeAll
    static void startServer() throws InterruptedException {
        new Thread(() -> {
            server.post("/transform", ctx -> HttpResponse.of(ctx.getBody()).transform(s -> s + "ed"));
            server.post("/echo", ctx -> HttpResponse.of(ctx.getBody()));
            server.start();
        }).start();

        // Wait for server to start
        TimeUnit.SECONDS.sleep(3);
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }

    @RepeatedTest(2)
    @DisplayName("Transformer")
    void transformer() throws IOException, InterruptedException {
        val res = HttpClient.builder()
                .url(url + "/transform").method("POST").body("transform")
                .build().perform();
        assertEquals("transformed", res.getBody());
        // TODO: fix loop error
        TimeUnit.MILLISECONDS.sleep(100);
    }
}
