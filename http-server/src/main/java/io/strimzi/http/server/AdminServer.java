package io.strimzi.http.server;

import io.strimzi.http.server.api.RouteRegistration;
import io.strimzi.http.server.api.RouteRegistrationDescriptor;
import io.strimzi.http.server.logging.Utils;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.web.Router;
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AdminServer extends AbstractVerticle {
    private static final Logger LOGGER = LoggerFactory.getLogger(AdminServer.class);

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
            .onFailure(throwable -> {
                LOGGER.error("Loading of routes was unsuccessful.");
                LOGGER.error(Utils.formatStacktrace(throwable));
            });
    }

    private Future<Router> loadRoutes() {

        final Router router = Router.router(vertx);
        final ServiceLoader<RouteRegistration> loader = ServiceLoader.load(RouteRegistration.class);
        final List<Future<RouteRegistrationDescriptor>> modules = new ArrayList<>();

        loader.forEach(routerRegistration -> modules.add(routerRegistration.registerRoutes(vertx)));

        return CompositeFuture.all(new ArrayList<>(modules))
            .onSuccess(cf -> modules.forEach(future -> {
                final String mountPoint = future.result().mountPoint();
                final Router subRouter = future.result().router();

                router.mountSubRouter(mountPoint, subRouter);

            })).map(router);
    }
}
