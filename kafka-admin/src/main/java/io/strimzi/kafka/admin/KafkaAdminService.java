package io.strimzi.kafka.admin;

import io.strimzi.http.server.api.RouteRegistration;
import io.strimzi.http.server.api.RouteRegistrationDescriptor;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.api.contract.openapi3.OpenAPI3RouterFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KafkaAdminService implements RouteRegistration {

    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaAdminService.class);

    @Override
    public Future<RouteRegistrationDescriptor> registerRoutes(final Vertx vertx) {

        final Promise<RouteRegistrationDescriptor> promise = Promise.promise();

        OpenAPI3RouterFactory.create(vertx, "openapi-specs/kafka-admin.yaml",
            ar -> createRouterFactory(ar, promise));

        return promise.future();
    }

    private void createRouterFactory(final AsyncResult<OpenAPI3RouterFactory> ar, final Promise<RouteRegistrationDescriptor> registerPromise) {

        if (ar.succeeded()) {
            final OpenAPI3RouterFactory routerFactory = ar.result();

            routerFactory.addHandlerByOperationId("ListTopics", this::listTopics);
            routerFactory.addHandlerByOperationId("GetTopic", this::getTopic);

            registerPromise.complete(RouteRegistrationDescriptor.create("/kafka", routerFactory.getRouter()));
            LOGGER.info("Endpoint '/kafka/topics' registered.");
            LOGGER.info("Endpoint '/kafka/topics/{topic-name}' registered.");
        } else {
            registerPromise.fail(ar.cause());
        }
    }

    private void listTopics(final RoutingContext routingContext) {
        final JsonArray topics = new JsonArray()
            .add("topic1")
            .add("topic2")
            .add("topic3");
        routingContext.response().end(topics.encodePrettily());
    }

    private void getTopic(final RoutingContext routingContext) {
        final JsonObject topic = new JsonObject()
            .put("name", "topic1")
            .put("partition_count", 3)
            .put("replication_factor", 3)
            .put("is_internal", false);
        routingContext.response().end(topic.encodePrettily());
    }
}

