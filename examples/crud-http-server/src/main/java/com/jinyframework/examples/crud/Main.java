package com.jinyframework.examples.crud;

import com.jinyframework.HttpServer;
import com.jinyframework.core.RequestBinderBase.HttpResponse;
import com.jinyframework.examples.crud.factories.AppFactory;
import com.jinyframework.examples.crud.router.CatRouter;
import com.jinyframework.examples.crud.router.DogRouter;
import lombok.val;

import java.io.IOException;

public final class Main {
    public static void main(String[] args) throws IOException {
        val server = HttpServer.port(1234);
        server.useTransformer(res -> AppFactory.getGson().toJson(res));

        server.get("/", ctx -> HttpResponse.of("CRUD"));
        val catRouter = CatRouter.getRouter();
        val dogRouter = DogRouter.getRouter();
        server.use("/cats", catRouter);
        server.use("/dogs", dogRouter);

        server.start();
    }
}