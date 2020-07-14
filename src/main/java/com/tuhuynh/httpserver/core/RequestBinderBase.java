package com.tuhuynh.httpserver.core;

import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import com.tuhuynh.httpserver.core.RequestUtils.RequestMethod;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.val;
import lombok.var;

@RequiredArgsConstructor
public class RequestBinderBase {
    protected final RequestContext requestContext;

    protected BinderInitObject binderInit(final BaseHandlerMetadata<?> h) {
        val indexOfQuestionMark = requestContext.getPath().indexOf('?');
        var requestPath =
                indexOfQuestionMark == -1 ? requestContext.getPath() : requestContext.getPath().substring(0,
                                                                                                          indexOfQuestionMark);
        // Remove all last '/' from the requestPath
        while (requestPath.endsWith("/")) {
            requestPath = requestPath.substring(0, requestPath.length() - 1);
        }

        val handlerPathOriginal = h.getPath();
        val handlerPathArrWithHandlerParams = Arrays.stream(handlerPathOriginal.split("/"));
        val handlerPath = handlerPathArrWithHandlerParams.filter(e -> !e.startsWith(":")).collect(
                Collectors.joining("/"));

        val numOfHandlerParams = handlerPathOriginal.length() - handlerPathOriginal.replace(":", "")
                                                                                   .length();
        val numOfSlashOfRequestPath = requestPath.length() - requestPath.replace("/", "").length();
        val numOfSlashOfHandlerPath = handlerPathOriginal.length() - handlerPathOriginal.replace("/", "")
                                                                                        .length();

        val requestWithHandlerParamsMatched = numOfHandlerParams > 0 && requestPath.startsWith(handlerPath)
                                              && numOfSlashOfRequestPath == numOfSlashOfHandlerPath;

        if (requestWithHandlerParamsMatched) {
            val elementsOfHandlerPath = handlerPathOriginal.split("/");
            val elementsOfRequestPath = requestPath.split("/");
            for (int i = 1; i < elementsOfHandlerPath.length; i++) {
                if (elementsOfHandlerPath[i].startsWith(":")) {
                    val handlerParamKey = elementsOfHandlerPath[i].replace(":", "");
                    val handlerParamValue = elementsOfRequestPath[i];
                    requestContext.getParam().put(handlerParamKey, handlerParamValue);
                }
            }
        }

        return BinderInitObject.builder()
                               .requestPath(requestPath)
                               .handlerPath(handlerPath)
                               .requestWithHandlerParamsMatched(requestWithHandlerParamsMatched)
                               .build();
    }

    public interface RequestHandlerBase {
    }

    @FunctionalInterface
    public interface RequestHandlerBIO extends RequestHandlerBase {
        HttpResponse handleFunc(RequestContext requestMetadata) throws Exception;
    }

    @FunctionalInterface
    public interface RequestHandlerNIO extends RequestHandlerBase {
        CompletableFuture<HttpResponse> handleFunc(RequestContext requestMetadata) throws Exception;
    }

    @Builder
    @Getter
    protected static class BinderInitObject {
        String requestPath;
        String handlerPath;
        boolean requestWithHandlerParamsMatched;
    }

    @Getter
    @Builder
    public static final class RequestContext {
        private RequestMethod method;
        private String path;
        private HashMap<String, String> header;
        private String body;
        private HashMap<String, String> query;
        private HashMap<String, String> param;
        private HashMap<String, String> data;

        public void putHandlerData(final String key, final String value) {
            data.put(key, value);
        }

        public String getData(final String key) {
            return data.get(key);
        }
    }

    @AllArgsConstructor
    @RequiredArgsConstructor
    @Getter
    public abstract static class BaseHandlerMetadata<T extends RequestHandlerBase> {
        public RequestMethod method;
        public String path;
        public T[] handlers;
    }

    @Getter
    public static class BIOHandlerMetadata extends BaseHandlerMetadata<RequestHandlerBIO> {
        public BIOHandlerMetadata(final RequestMethod method, final String path,
                                  final RequestHandlerBIO[] handlers) {
            super(method, path, handlers);
        }
    }

    @Getter
    public static class NIOHandlerMetadata extends BaseHandlerMetadata<RequestHandlerNIO> {
        public NIOHandlerMetadata(final RequestMethod method, final String path,
                                  final RequestHandlerNIO[] handlers) {
            super(method, path, handlers);
        }
    }

    @Getter
    public static final class HttpResponse {
        public static HttpResponse next() { return new HttpResponse(0, "", true); }

        public static HttpResponse reject(final String errorText) {
            return new HttpResponse(400, errorText, false);
        }

        public static CompletableFuture<HttpResponse> of(
                final CompletableFuture<HttpResponse> completableFuture) {
            return completableFuture;
        }

        public static <T> HttpResponse of(final T t) {
            return new HttpResponse(200, t.toString(), true);
        }

        private int httpStatusCode;
        private String responseString;
        private boolean allowNext;

        private HttpResponse(final int httpStatusCode, final String responseString, final boolean allowNext) {
            this.httpStatusCode = httpStatusCode;
            this.responseString = responseString;
            this.allowNext = allowNext;
        }

        public HttpResponse status(final int httpStatusCode) {
            this.httpStatusCode = httpStatusCode;
            return this;
        }
    }

}
