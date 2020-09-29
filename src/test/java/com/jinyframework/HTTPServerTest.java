package com.jinyframework;

import com.jinyframework.core.RequestBinderBase.HttpResponse;
import com.jinyframework.core.bio.HttpRouter;
import lombok.val;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@DisplayName("api.HttpServerTest")
public class HTTPServerTest extends HTTPTest {
    public HTTPServerTest() {
        this.url = "http://localhost:1234";
    }

    @BeforeAll
    static void startServer() throws InterruptedException {
        new Thread(() -> {
            val server = HttpServer.port(1234);

            server.use(ctx -> {
                ctx.getData().put("global", "middleware");
                return HttpResponse.next();
            });

            server.get("/", ctx -> HttpResponse.of("Hello World"));
            server.post("/transform", ctx -> HttpResponse.of(ctx.getBody()).transform(s -> s + "ed"));
            server.get("/gm", ctx -> HttpResponse.of(ctx.getData().get("global")));
            server.get("/gm-sub", ctx -> HttpResponse.of(ctx.getData().get("att")));
            server.post("/echo", ctx -> HttpResponse.of(ctx.getBody()));
            server.get("/query", ctx -> {
                val world = ctx.getQuery().get("hello");
                return HttpResponse.of(world);
            });
            server.get("/path/:foo/:bar", ctx -> {
                val foo = ctx.getParam().get("foo");
                val bar = ctx.getParam().get("bar");
                return HttpResponse.of(foo + ":" + bar);
            });
            server.get("/all/**", ctx -> HttpResponse.of(ctx.getPath()));

            server.get("/protected",
                    ctx -> {
                        val authorizationHeader = ctx.getHeader().get("authorization");
                        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer")) {
                            return HttpResponse.reject("invalid_token").status(401);
                        }
                        ctx.getData().put("username", "tuhuynh");
                        return HttpResponse.next();
                    }, // Injected
                    ctx -> HttpResponse.of("success:" + ctx.getData().get("username")));

            server.get("/panic", ctx -> {
                throw new RuntimeException("Panicked!");
            });

            val catRouter = new HttpRouter();
            catRouter.use(ctx -> {
                ctx.getData().put("att", "cat");
                return HttpResponse.next();
            });
            catRouter.get("/", ctx -> HttpResponse.of("this is a cat"));
            catRouter.get("/gm", ctx -> HttpResponse.of(ctx.getData().get("att")));
            catRouter.post("/echo", ctx -> HttpResponse.of(ctx.getBody()));
            catRouter.get("/:foo/:bar", ctx -> HttpResponse.of(ctx.getParam().get("foo") + ":" + ctx.getParam().get("bar")));
            server.use("/cat", catRouter);

            try {
                server.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();

        // Wait for server to start
        TimeUnit.SECONDS.sleep(3);
    }
}
