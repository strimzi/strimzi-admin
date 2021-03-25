/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package io.strimzi.admin.http.server;

import io.strimzi.admin.http.server.registration.RouteRegistration;
import io.strimzi.admin.http.server.registration.RouteRegistrationDescriptor;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.web.Router;
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

import io.vertx.micrometer.MicrometerMetricsOptions;
import io.vertx.micrometer.VertxPrometheusOptions;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * The main Strimzi Admin Server class. It is a Vert.x {@link io.vertx.core.Verticle} and it starts
 * an HTTP server which listen for inbound HTTP requests.
 * <p>
 * The resources that are defined to the server are loaded using the Java Service loader
 * mechanism. All modules implementing the {@link RouteRegistration} interface are loaded and the
 * getRegistrationDescriptor method called to get the set of routes and the mount point. The routes
 * are added to the server Router at the mount point.
 */
public class AdminServer extends AbstractVerticle {
    private static final Logger LOGGER = LogManager.getLogger(AdminServer.class);

    @Override
    public void start(final Promise<Void> startServer) {
        VertxOptions options = new VertxOptions().setMetricsOptions(
                new MicrometerMetricsOptions()
                        .setPrometheusOptions(new VertxPrometheusOptions().setEnabled(true))
                        .setJvmMetricsEnabled(true)
                        .setEnabled(true));
        vertx = Vertx.vertx(options);


        loadRoutes()
            .onSuccess(router -> {
                final HttpServer server = vertx.createHttpServer();
                server.requestHandler(router).listen(8080);
                LOGGER.info("Admin Server is listening on port 8080");
            })
            .onFailure(throwable -> LOGGER.atFatal().withThrowable(throwable).log("Loading of routes was unsuccessful."));
    }

    private Future<Router> loadRoutes() {
        final Router router = Router.router(vertx);
        final ServiceLoader<RouteRegistration> loader = ServiceLoader.load(RouteRegistration.class);
        final List<Future<RouteRegistrationDescriptor>> routeRegistrationDescriptors = new ArrayList<>();

        loader.forEach(routeRegistration -> routeRegistrationDescriptors.add(routeRegistration.getRegistrationDescriptor(vertx)));

        return CompositeFuture.all(new ArrayList<>(routeRegistrationDescriptors))
            .onSuccess(cf -> routeRegistrationDescriptors.forEach(future -> {
                final String mountPoint = future.result().mountPoint();
                final Router subRouter = future.result().router();

                router.mountSubRouter(mountPoint, subRouter);

                LOGGER.info("Module routes mounted on path '{}'.", mountPoint);
            })).map(router);
    }
}
