package io.strimzi.http.server;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.web.Router;
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.javatuples.Triplet;

public class AdminServer extends AbstractVerticle {
    private static final Logger LOGGER = LogManager.getLogger(AdminServer.class);

    @Override
    public void start(final Promise<Void> startServer) {

        loadRoutes()
            .onSuccess(routes -> {
                final HttpServer server = vertx.createHttpServer();
                final Router router = Router.router(vertx);
                router.mountSubRouter("/", routes);   // Mount the sub router containing the module routes
                server.requestHandler(router).listen(8080);
                LOGGER.info("AdminServer is listening on port 8080");
            })
            .onFailure(throwable -> LOGGER.atFatal().withThrowable(throwable).log("Loading of routes was unsuccessful."));
    }

    private Future<Router> loadRoutes() {

        final Router router = Router.router(vertx);
        final ServiceLoader<RestService> loader = ServiceLoader.load(RestService.class);
        final List<Future<Triplet<String, String, Router>>> modules = new ArrayList<>();

        loader.forEach(restService -> modules.add(restService.registerRoutes(vertx)));

        return CompositeFuture.all(new ArrayList<>(modules))
            .onSuccess(cf -> modules.forEach(future -> {
                final String moduleName = future.result().getValue0();
                final String mountPoint = future.result().getValue1();
                final Router subRouter = future.result().getValue2();

                router.mountSubRouter(mountPoint, subRouter);

                LOGGER.info("Module {} mounted on path {}.", moduleName, mountPoint);

            })).map(router);
    }
}
