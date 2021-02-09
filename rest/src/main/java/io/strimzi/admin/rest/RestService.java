/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package io.strimzi.admin.rest;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.strimzi.admin.http.server.registration.RouteRegistration;
import io.strimzi.admin.http.server.registration.RouteRegistrationDescriptor;
import io.strimzi.admin.kafka.admin.HttpMetrics;
import io.strimzi.admin.kafka.admin.KafkaAdmin;
import io.strimzi.admin.kafka.admin.handlers.RestOperations;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.ext.web.api.contract.openapi3.OpenAPI3RouterFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Implements routes to be used as kubernetes liveness and readiness probes. The implementations
 * simply return a static string containing a JSON body of "status: ok".
 */
public class RestService implements RouteRegistration {

    protected final Logger log = LogManager.getLogger(RestService.class);
    KafkaAdmin ka;
    HttpMetrics httpMetrics = new HttpMetrics();

    @Override
    public Future<RouteRegistrationDescriptor> getRegistrationDescriptor(final Vertx vertx) {

        final Promise<RouteRegistrationDescriptor> promise = Promise.promise();

        OpenAPI3RouterFactory.create(vertx, "openapi-specs/rest.yaml", ar -> {
            if (ar.succeeded()) {
                try {
                    ka = new KafkaAdmin();
                } catch (Exception e) {
                    promise.fail(e);
                }
                OpenAPI3RouterFactory routerFactory = ar.result();
                assignRoutes(routerFactory, vertx);
                promise.complete(RouteRegistrationDescriptor.create("/rest", routerFactory.getRouter()));
                log.info("Rest server started.");
            } else {
                promise.fail(ar.cause());
            }
        });

        return promise.future();
    }

    private void assignRoutes(final OpenAPI3RouterFactory routerFactory, final Vertx vertx) {
        RestOperations ro = new RestOperations();
        routerFactory.addHandlerByOperationId("getTopic", ro.describeTopic(ka.getAcConfig(), vertx, httpMetrics));
        routerFactory.addHandlerByOperationId("getTopicsList", ro.listTopics(ka.getAcConfig(), vertx, httpMetrics));

        routerFactory.addHandlerByOperationId("deleteTopic", ro.deleteTopic(ka.getAcConfig(), vertx, httpMetrics));
        routerFactory.addHandlerByOperationId("createTopic", ro.createTopic(ka.getAcConfig(), vertx, httpMetrics));
        routerFactory.addHandlerByOperationId("updateTopic", ro.updateTopic(ka.getAcConfig(), vertx, httpMetrics));

        routerFactory.addHandlerByOperationId("metrics", routingContext -> routingContext.response().setStatusCode(HttpResponseStatus.OK.code()).end(httpMetrics.getRegistry().scrape()));
    }
}
