/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package io.strimzi.admin.kafka.admin.handlers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.strimzi.admin.common.data.fetchers.AdminClientWrapper;
import io.strimzi.admin.common.data.fetchers.TopicOperations;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.graphql.VertxDataFetcher;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class TopicsDeleteHandler extends CommonHandler {
    protected static final Logger log = LogManager.getLogger(TopicsDeleteHandler.class);

    public static VertxDataFetcher deleteTopicsFetcher(Map<String, Object> acConfig, Vertx vertx) {
        VertxDataFetcher<List<String>> dataFetcher = new VertxDataFetcher<>((environment, prom) -> {
            setOAuthToken(acConfig, environment.getContext());
            Future<AdminClientWrapper> acw = createAdminClient(vertx, acConfig);

            List<String> topicsToDelete = environment.getArgument("names");
            TopicOperations.deleteTopics(acw, topicsToDelete, prom);
        });
        return dataFetcher;
    }

    public static Handler<RoutingContext> deleteTopicsHandler(Map<String, Object> acConfig, Vertx vertx) {
        return routingContext -> {
            setOAuthToken(acConfig, routingContext);
            Future<AdminClientWrapper> acw = createAdminClient(vertx, acConfig);


            List<String> topicsToDelete = Arrays.asList(routingContext.queryParams().get("names").split(",").clone());

            Promise<List<String>> prom = Promise.promise();
            TopicOperations.deleteTopics(acw, topicsToDelete, prom);

            prom.future().onComplete(res -> {
                if (res.failed()) {
                    routingContext.response().setStatusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.code());
                    routingContext.response().end(res.cause().getMessage());
                } else {
                    ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
                    String json = null;
                    try {
                        json = ow.writeValueAsString(res.result());
                    } catch (JsonProcessingException e) {
                        routingContext.response().setStatusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.code());
                        routingContext.response().end(e.getMessage());
                    }
                    routingContext.response().setStatusCode(HttpResponseStatus.OK.code());
                    routingContext.response().end(json);
                }
            });
        };
    }
}
