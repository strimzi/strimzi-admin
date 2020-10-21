package io.strimzi.admin.health;

import io.strimzi.http.server.api.RouteRegistration;
import io.strimzi.http.server.api.RouteRegistrationDescriptor;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.ext.web.api.contract.openapi3.OpenAPI3RouterFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Health implements RouteRegistration {

    private static final Logger LOGGER = LoggerFactory.getLogger(Health.class);
    private static final String SUCCESS_RESPONSE = "{\"status\": \"OK\"}";

    @Override
    public Future<RouteRegistrationDescriptor> registerRoutes(final Vertx vertx) {

        final Promise<RouteRegistrationDescriptor> promise = Promise.promise();

        OpenAPI3RouterFactory.create(vertx, "openapi-specs/health.yaml",
            ar -> createRouterFactory(ar, promise));

        return promise.future();
    }

    private void createRouterFactory(final AsyncResult<OpenAPI3RouterFactory> ar, final Promise<RouteRegistrationDescriptor> registerPromise) {

        if (ar.succeeded()) {
            final OpenAPI3RouterFactory routerFactory = ar.result();

            routerFactory.addHandlerByOperationId("status", rc -> rc.response().end(SUCCESS_RESPONSE));
            routerFactory.addHandlerByOperationId("liveness", rc -> rc.response().end(SUCCESS_RESPONSE));

            registerPromise.complete(RouteRegistrationDescriptor.create("/health", routerFactory.getRouter()));
            LOGGER.info("Endpoint '/health/status' registered.");
            LOGGER.info("Endpoint '/health/liveness' registered.");
        } else {
            registerPromise.fail(ar.cause());
        }

    }
}
