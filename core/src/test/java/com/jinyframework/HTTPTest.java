package com.jinyframework;

import lombok.val;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

public abstract class HTTPTest {
    protected String url = "";
    protected boolean isCI =
            System.getenv("CI") != null
                    && System.getenv("CI").equalsIgnoreCase("true");

    @Test
    @DisplayName("Hello World")
    void helloWorld() throws IOException {
        val res = HttpClient.builder()
                .url(url + "/").method("GET")
                .build().perform();
        assertEquals("Hello World", res.getBody());
    }

    @Test
    @DisplayName("Default Response Headers")
    void defaultResponseHeaders() throws IOException {
        val res = HttpClient.builder()
                .url(url + "/").method("GET")
                .build().perform();
        assertEquals("application/json", res.getHeader("Content-Type"));
    }

    @Test
    @DisplayName("Immediately return from handler chain")
    void immediateReturn() throws IOException {
        val res = HttpClient.builder()
                .url(url + "/immediate").method("GET")
                .build().perform();
        assertEquals("Foo", res.getBody());
    }

    // Temporary disable: CI error
    @Test
    @DisplayName("Transformer")
    void transformer() throws IOException {
        val res = HttpClient.builder()
                .url(url + "/transform").method("POST").body("transform")
                .build().perform();
        assertEquals("transformed", res.getBody());
    }

    @Test
    @DisplayName("Echo")
    void echo() throws IOException {
        val res = HttpClient.builder()
                .url(url + "/echo").method("POST").body("copycat")
                .build().perform();
        assertEquals("copycat", res.getBody());
    }

    @Test
    @DisplayName("Response Header")
    void header() throws IOException {
        val res = HttpClient.builder()
                .url(url + "/header").method("GET")
                .build().perform();
        assertEquals("bar", res.getHeader("foo"));
    }

    @Test
    @DisplayName("Header Params")
    void headerParams() throws IOException {
        val res = HttpClient.builder()
                .url(url + "/req-header").method("GET")
                .header("Foo", "foo").header("Bar", "bar")
                .build().perform();
        assertEquals("foobar", res.getBody());
    }

    @Test
    @DisplayName("Query Params")
    void queryParams() throws IOException {
        val res = HttpClient.builder()
                .url(url + "/query?hello=world").method("GET")
                .build().perform();
        assertEquals(res.getBody(), "world");
    }

    @Test
    @DisplayName("Path Params")
    void pathParams() throws IOException {
        val res = HttpClient.builder()
                .url(url + "/path/hello/123").method("GET")
                .build().perform();
        assertEquals("hello:123", res.getBody());
    }

    @Test
    @DisplayName("Data Params")
    void dataParams() throws IOException {
        val res = HttpClient.builder()
                .url(url + "/data/param").method("GET")
                .build().perform();
        assertEquals("ok", res.getBody());
    }

    @Test
    @DisplayName("Catch All")
    void catchAll() throws IOException {
        val res = HttpClient.builder()
                .url(url + "/all/123/456").method("GET")
                .build().perform();
        assertEquals("/all/123/456", res.getBody());
    }

    @Test
    @DisplayName("Middleware failed")
    void middlewareFail() throws IOException {
        val res = HttpClient.builder()
                .url(url + "/protected").method("GET")
                .build().perform();
        assertEquals(res.getStatus(), 401);
        assertEquals(res.getBody(), "invalid_token");
    }

    @Test
    @DisplayName("Middleware success")
    void middlewareSuccess() throws IOException {
        val headers = new HashMap<String, String>();
        headers.put("Authorization", "Bearer test_token");
        val res = HttpClient.builder()
                .url(url + "/protected").method("GET").headers(headers)
                .build().perform();
        assertEquals(200, res.getStatus());
        assertEquals("success:tuhuynh", res.getBody());
    }

    @Test
    @DisplayName("Panic")
    void panic() throws IOException {
        val res = HttpClient.builder()
                .url(url + "/panic").method("GET")
                .build().perform();
        assertEquals(500, res.getStatus());
        assertEquals("Panicked!", res.getBody());
    }

    @Test
    @DisplayName("Path case")
    void pathCase() throws IOException {
        val hasOk = HttpClient.builder()
                .url(url + "/hasCase").method("GET")
                .build().perform();
        assertEquals(hasOk.getStatus(), 200);
        val hasFail = HttpClient.builder()
                .url(url + "/hascase").method("GET")
                .build().perform();
        assertEquals(hasFail.getStatus(),404);
        val nonOk = HttpClient.builder()
                .url(url + "/noncase").method("GET")
                .build().perform();
        assertEquals(nonOk.getStatus(), 200);
        val nonFail = HttpClient.builder()
                .url(url + "/nonCase").method("GET")
                .build().perform();
        assertEquals(nonFail.getStatus(), 404);
    }

    @Test
    @DisplayName("Global Middleware")
    void globalMiddleware() throws IOException {
        val res = HttpClient.builder()
                .url(url + "/gm").method("GET")
                .build().perform();
        assertEquals("middleware", res.getBody());
    }

    @Test
    @DisplayName("Global Middleware not from subRouter")
    void globalMiddlewareNotFromSubRouter() throws IOException {
        val res = HttpClient.builder()
                .url(url + "/gm-sub").method("GET")
                .build().perform();
        assertEquals("", res.getBody());
    }

    @Test
    @DisplayName("SubRouter1")
    void subRouter1() throws IOException {
        val res = HttpClient.builder()
                .url(url + "/cat").method("GET")
                .build().perform();
        assertEquals("this is a cat", res.getBody());
    }

    @Test
    @DisplayName("SubRouter2")
    void subRouter2() throws IOException {
        val res = HttpClient.builder()
                .url(url + "/cat/test").method("POST")
                .body("catTest")
                .build().perform();
        assertEquals("catTest", res.getBody());
    }

    @Test
    @DisplayName("SubRouter3")
    void subRouter3() throws IOException {
        val res = HttpClient.builder()
                .url(url + "/cat/gm").method("GET")
                .build().perform();
        assertEquals(res.getBody(), "cat");
    }

    @Test
    @DisplayName("SubRouter4")
    void subRouter4() throws IOException {
        val res = HttpClient.builder()
                .url(url + "/cat/foo/bar").method("GET")
                .build().perform();
        assertEquals("foo:bar", res.getBody());
    }

    @AfterEach
    void each() throws InterruptedException {
        TimeUnit.MILLISECONDS.sleep(100);
    }
}
