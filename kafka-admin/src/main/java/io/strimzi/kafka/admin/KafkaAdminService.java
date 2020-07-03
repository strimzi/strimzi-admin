package io.strimzi.kafka.admin;

import io.strimzi.http.server.RestService;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.api.contract.openapi3.OpenAPI3RouterFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.javatuples.Triplet;

public class KafkaAdminService implements RestService {

    private static final Logger LOGGER = LogManager.getLogger(KafkaAdminService.class);

    @Override
    public Future<Triplet<String, String, Router>> registerRoutes(final Vertx vertx) {

        final Promise<Triplet<String, String, Router>> promise = Promise.promise();

        OpenAPI3RouterFactory.create(vertx, "openapi-specs/kafka-admin.yaml",
            ar -> createRouterFactory(ar, promise));

        return promise.future();
    }

    private void createRouterFactory(final AsyncResult<OpenAPI3RouterFactory> ar, final Promise<Triplet<String, String, Router>> registerPromise) {

        if (ar.succeeded()) {
            final OpenAPI3RouterFactory routerFactory = ar.result();

            routerFactory.addHandlerByOperationId("ListTopics", this::listTopics);
            routerFactory.addHandlerByOperationId("GetTopic", this::getTopic);

            registerPromise.complete(Triplet.with(KafkaAdminService.class.getName(), "/kafka", routerFactory.getRouter()));
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

