package io.strimzi.health;

import io.strimzi.http.server.RestService;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.api.contract.openapi3.OpenAPI3RouterFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.javatuples.Triplet;

public class HealthService implements RestService {

    private static final Logger LOGGER = LogManager.getLogger(HealthService.class);
    private static final String SUCCESS_RESPONSE = "{\"status\": \"OK\"}";

    @Override
    public Future<Triplet<String, String, Router>> registerRoutes(final Vertx vertx) {

        final Promise<Triplet<String, String, Router>> promise = Promise.promise();

        OpenAPI3RouterFactory.create(vertx, "openapi-specs/health.yaml",
            ar -> createRouterFactory(ar, promise));

        return promise.future();
    }

    private void createRouterFactory(final AsyncResult<OpenAPI3RouterFactory> ar, final Promise<Triplet<String, String, Router>> registerPromise) {

        if (ar.succeeded()) {
            final OpenAPI3RouterFactory routerFactory = ar.result();

            routerFactory.addHandlerByOperationId("status", rc -> rc.response().end(SUCCESS_RESPONSE));
            routerFactory.addHandlerByOperationId("liveness", rc -> rc.response().end(SUCCESS_RESPONSE));

            registerPromise.complete(Triplet.with(HealthService.class.getName(), "/health", routerFactory.getRouter()));
            LOGGER.info("Endpoint '/health/status' registered.");
            LOGGER.info("Endpoint '/health/liveness' registered.");
        } else {
            registerPromise.fail(ar.cause());
        }

    }
}
