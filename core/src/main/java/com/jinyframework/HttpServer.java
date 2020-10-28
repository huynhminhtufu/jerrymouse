package com.jinyframework;

import com.jinyframework.core.AbstractHttpRouter;
import com.jinyframework.core.AbstractRequestBinder.Handler;
import com.jinyframework.core.AbstractRequestBinder.RequestTransformer;
import com.jinyframework.core.bio.RequestPipeline;
import com.jinyframework.core.factories.ServerThreadFactory;
import com.jinyframework.core.utils.Intro;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * The type Http server.
 */
@Slf4j
public final class HttpServer extends AbstractHttpRouter<Handler> {
    private final int serverPort;
    private final ServerThreadFactory threadFactory = new ServerThreadFactory("request-processor");
    private final Executor executor = Executors.newCachedThreadPool(threadFactory);
    private ServerSocket serverSocket;

    private HttpServer(final int serverPort) {
        this.serverPort = serverPort;
    }

    /**
     * Port http server.
     *
     * @param serverPort the server port
     * @return the http server
     */
    public static HttpServer port(final int serverPort) {
        return new HttpServer(serverPort);
    }

    /**
     * Use transformer http server.
     *
     * @param transformer the transformer
     * @return the http server
     */
    public HttpServer useTransformer(@NonNull final RequestTransformer transformer) {
        this.transformer = transformer;
        return this;
    }

    /**
     * Use response headers http server.
     *
     * @param responseHeaders the response headers
     * @return the http server
     */
    public HttpServer useResponseHeaders(@NonNull final Map<String, String> responseHeaders) {
        this.responseHeaders = responseHeaders;
        return this;
    }

    /**
     * Sets thread debug mode.
     *
     * @param isDebug the is debug
     * @return the thread debug mode
     */
    public HttpServer setThreadDebugMode(final boolean isDebug) {
        threadFactory.setDebug(isDebug);
        return this;
    }

    /**
     * Start.
     *
     * @throws IOException the io exception
     */
    public void start() throws IOException {
        Intro.begin();
        serverSocket = new ServerSocket();
        serverSocket.bind(new InetSocketAddress(InetAddress.getLoopbackAddress(), serverPort));
        log.info("Started Jiny HTTP Server on port " + serverPort);
        while (!Thread.interrupted()) {
            val clientSocket = serverSocket.accept();
            executor.execute(
                    new RequestPipeline(clientSocket, middlewares, handlers, responseHeaders, transformer));
        }
    }

    /**
     * Stop.
     *
     * @throws IOException the io exception
     */
    public void stop() throws IOException {
        if (!serverSocket.isClosed()) {
            serverSocket.close();
            log.info("Stopped Jiny HTTP Server on port " + serverPort);
        }
    }
}
