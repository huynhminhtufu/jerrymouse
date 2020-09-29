package com.jinyframework.core.utils;

import com.jinyframework.core.RequestBinderBase.HttpResponse;
import com.jinyframework.core.utils.ParserUtils.HttpMethod;
import lombok.val;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("core.ParserUtilsTest")
public class ParserUtilsTest {
    @Test
    @DisplayName("Parse Request")
    void parseRequestTest() {
        final String[] request = {"GET /test HTTP/1.1", "Host: localhost", "User-Agent: Mozilla/5.0"};
        val body = "SampleBody";
        val context = ParserUtils.parseRequest(request, body);
        assertEquals("/test", context.getPath(), "Get Path");
        assertEquals(HttpMethod.GET, context.getMethod(), "Get Method");
        assertEquals("Mozilla/5.0", context.getHeader().get("user-agent"), "Get Header");
        assertEquals("SampleBody", context.getBody(), "Get Body");
    }

    @Test
    @DisplayName("Parse Response")
    void parseResponseTest() {
        val response = HttpResponse.of("Hello World");
        assertEquals(200, response.getHttpStatusCode(), "Get HTTP Status Code");
        val responseString = ParserUtils.parseResponse(response, Object::toString);
        assertEquals("HTTP/1.1 200 OK\n\nHello World\n", responseString, "Get response string");
    }

    @Test
    @DisplayName("Split Query")
    void splitQuery() {
        val queryStr = "foo=bar&hello=world&x";
        val result = ParserUtils.splitQuery(queryStr);
        assertEquals(result.get("foo"), "bar");
        assertEquals(result.get("hello"), "world");
        assertEquals(result.get("x"), "");
    }
}
