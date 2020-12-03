/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package io.strimzi.admin.health;

import io.strimzi.admin.http.server.registration.RouteRegistration;
import io.strimzi.admin.http.server.registration.RouteRegistrationDescriptor;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.ext.web.api.contract.openapi3.OpenAPI3RouterFactory;

import java.util.Map;

/**
 * Implements routes to be used as kubernetes liveness and readiness probes. The implementations
 * simply return a static string containing a JSON body of "status: ok".
 */
public class HealthService implements RouteRegistration {

    private static final String SUCCESS_RESPONSE = "{\"status\": \"OK\"}";

    @Override
    public Future<RouteRegistrationDescriptor> getRegistrationDescriptor(final Vertx vertx, Map<String, Object> config) {

        final Promise<RouteRegistrationDescriptor> promise = Promise.promise();

        OpenAPI3RouterFactory.create(vertx, "openapi-specs/health.yaml", ar -> {
            if (ar.succeeded()) {
                OpenAPI3RouterFactory routerFactory = ar.result();
                assignRoutes(routerFactory);
                promise.complete(RouteRegistrationDescriptor.create("/health", routerFactory.getRouter()));
            } else {
                promise.fail(ar.cause());
            }
        });

        return promise.future();
    }

    private void assignRoutes(final OpenAPI3RouterFactory routerFactory) {
        routerFactory.addHandlerByOperationId("status", rc -> rc.response().end(SUCCESS_RESPONSE));
        routerFactory.addHandlerByOperationId("liveness", rc -> rc.response().end(SUCCESS_RESPONSE));
    }
}
