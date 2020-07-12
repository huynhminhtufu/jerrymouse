package com.tuhuynh.httpserver.tests;

import java.io.IOException;
import java.util.Random;

import com.tuhuynh.httpserver.HTTPClient;
import com.tuhuynh.httpserver.HTTPClient.ResponseObject;
import com.tuhuynh.httpserver.HTTPServer;
import com.tuhuynh.httpserver.handlers.HandlerBinder.HttpResponse;
import com.tuhuynh.httpserver.utils.HandlerUtils.RequestMethod;

public final class TestServer {
    public static void main(String[] args) throws IOException {
        final HTTPServer server = new HTTPServer(8080);

        server.addHandler(RequestMethod.GET, "/", ctx -> HttpResponse.of("Hello World"));
        server.addHandler(RequestMethod.POST, "/echo", ctx -> HttpResponse.of(ctx.getPayload()));

        // Free to execute blocking tasks with a Cached ThreadPool
        server.addHandler(RequestMethod.GET, "/sleep", ctx -> {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                System.out.println(e.getMessage());
            }
            return HttpResponse.of("Sleep done!");
        });

        server.addHandler(RequestMethod.GET, "/thread",
                          ctx -> HttpResponse.of(Thread.currentThread().getName()));

        server.addHandler(RequestMethod.GET, "/random", ctx -> {
            final Random rand = new Random();
            return HttpResponse.of(String.valueOf(rand.nextInt(100 + 1)));
        });

        // Get query params, ex: /query?hello=world
        server.addHandler(RequestMethod.GET, "/query", ctx -> {
            String world = ctx.getQueryParams().get("hello");
            String count = ctx.getQueryParams().get("count");
            return HttpResponse.of("Hello: " + world + ", Count: " + count);
        });

        // Perform as a proxy server
        server.addHandler(RequestMethod.GET, "/meme", ctx -> {
            // Built-in HTTP Client
            final ResponseObject
                    meme = HTTPClient.builder()
                                     .url("https://meme-api.herokuapp.com/gimme").method("GET")
                                     .build().perform();
            return HttpResponse.of(meme.getBody());
        });

        // Handle error
        server.addHandler(RequestMethod.GET, "/panic", ctx -> {
            throw new Exception("Panicked!");
        });

        server.start();
    }
}
