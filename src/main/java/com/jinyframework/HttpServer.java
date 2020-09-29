package com.jinyframework;

import com.jinyframework.core.RequestBinderBase.Handler;
import com.jinyframework.core.RequestBinderBase.RequestTransformer;
import com.jinyframework.core.HttpRouterBase;
import com.jinyframework.core.ServerThreadFactory;
import com.jinyframework.core.bio.RequestPipeline;
import lombok.val;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public final class HttpServer extends HttpRouterBase<Handler> {
    private final int serverPort;
    private final Executor executor = Executors.newCachedThreadPool(
            new ServerThreadFactory("request-processor"));
    private ServerSocket serverSocket;

    private RequestTransformer transformer = Object::toString;

    private HttpServer(final int serverPort) {
        this.serverPort = serverPort;
    }

    public static HttpServer port(final int serverPort) {
        return new HttpServer(serverPort);
    }

    public void setupResponseTransformer(RequestTransformer transformer) {
        this.transformer = transformer;
    }

    public void start() throws IOException {
        serverSocket = new ServerSocket();
        serverSocket.setReuseAddress(true);
        serverSocket.bind(new InetSocketAddress(InetAddress.getLoopbackAddress(), serverPort));
        System.out.println("Started HTTP Server on port " + serverPort);

        while (!serverSocket.isClosed()) {
            val socket = serverSocket.accept();
            executor.execute(new RequestPipeline(socket, middlewares, handlers, transformer));
        }
    }

    public void stop() throws IOException {
        if (!serverSocket.isClosed()) {
            serverSocket.close();
            System.out.println("Stopped HTTP Server on port " + serverPort);
        }
    }
}
