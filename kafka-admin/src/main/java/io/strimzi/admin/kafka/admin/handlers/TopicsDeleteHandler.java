/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package io.strimzi.admin.kafka.admin.handlers;

import io.strimzi.admin.kafka.admin.AdminClientProvider;
import io.vertx.core.Promise;
import io.vertx.ext.web.handler.graphql.VertxDataFetcher;
import io.vertx.ext.web.impl.RoutingContextWrapper;

import java.util.Arrays;
import java.util.List;

public class TopicsDeleteHandler {

    public static VertxDataFetcher deleteTopics(AdminClientProvider acp) {
        VertxDataFetcher<List<String>> dataFetcher = new VertxDataFetcher<>((environment, prom) -> {
            RoutingContextWrapper routingContext = environment.getContext();
            routingContext.request().headers().get("token");

            Promise deleteTopicPromise = Promise.promise();
            List<String> names = environment.getArgument("names");
            List<String> topicsToDelete = Arrays.asList(names.get(0).split(","));
            acp.deleteTopics(topicsToDelete, deleteTopicPromise);
            deleteTopicPromise.future().onComplete(topics -> {
                prom.complete(topicsToDelete);
            });
        });
        return dataFetcher;
    }
}
