/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package io.strimzi.admin.kafka.admin.handlers;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.strimzi.admin.kafka.admin.TopicOperations;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.graphql.VertxDataFetcher;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class TopicsDeleteHandler extends CommonHandler {
    protected static final Logger log = LogManager.getLogger(TopicsDeleteHandler.class);

    public static VertxDataFetcher deleteTopicsFetcher(Map<String, Object> acConfig, Vertx vertx) {
        VertxDataFetcher<List<String>> dataFetcher = new VertxDataFetcher<>((environment, prom) -> {
            setOAuthToken(acConfig, environment.getContext());

            List<String> topicsToDelete = environment.getArgument("names");
            createAdminClient(vertx, acConfig).onComplete(ac -> {
                if (ac.failed()) {
                    prom.fail(ac.cause());
                } else {
                    TopicOperations.deleteTopics(ac.result(), topicsToDelete, prom);
                }
            });
        });
        return dataFetcher;
    }

    public static Handler<RoutingContext> deleteTopicHandler(Map<String, Object> acConfig, Vertx vertx) {
        return routingContext -> {
            setOAuthToken(acConfig, routingContext);
            String uri = routingContext.request().uri();
            String topicToDelete = uri.substring(uri.lastIndexOf("/") + 1);
            Promise<List<String>> prom = Promise.promise();
            if (topicToDelete == null || topicToDelete.isEmpty()) {
                prom.fail("Topic to delete has not been specified.");
                processResponse(prom, routingContext, HttpResponseStatus.BAD_REQUEST);
                return;
            }

            createAdminClient(vertx, acConfig).onComplete(ac -> {
                if (ac.failed()) {
                    prom.fail(ac.cause());
                } else {
                    TopicOperations.deleteTopics(ac.result(), Collections.singletonList(topicToDelete), prom);
                }
                processResponse(prom, routingContext, HttpResponseStatus.OK);
            });
        };
    }
}
