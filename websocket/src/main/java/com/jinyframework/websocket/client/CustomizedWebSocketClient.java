package com.jinyframework.websocket.client;

import lombok.SneakyThrows;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.util.Map;

public class CustomizedWebSocketClient extends WebSocketClient {
    private final SocketEventHandler callback;

    public CustomizedWebSocketClient(URI serverUri, Map<String, String> headers, SocketEventHandler socketEventHandler) {
        super(serverUri);
        callback = socketEventHandler;
    }

    @SneakyThrows
    @Override
    public void onOpen(ServerHandshake handshakeData) {
        callback.onOpen(handshakeData);
    }

    @Override
    public void onMessage(String message) {
        callback.onMessage(message);
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        callback.onClose(code, reason, remote);
    }

    @Override
    public void onError(Exception ex) {
        if (ex != null && ex.getMessage() != null) {
            callback.onError(ex);
        }
    }

    public interface SocketEventHandler {
        void onOpen(ServerHandshake handshakeData) throws InterruptedException;

        void onMessage(String message);

        void onClose(int code, String reason, boolean remote);

        void onError(Exception ex);
    }
}
