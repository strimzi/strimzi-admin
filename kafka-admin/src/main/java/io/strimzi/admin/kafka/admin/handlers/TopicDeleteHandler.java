/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package io.strimzi.admin.kafka.admin.handlers;

import io.strimzi.admin.kafka.admin.AdminClientProvider;
import io.strimzi.admin.kafka.admin.model.Types;
import io.vertx.core.Promise;
import io.vertx.ext.web.handler.graphql.VertxDataFetcher;
import io.vertx.ext.web.impl.RoutingContextWrapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TopicDeleteHandler {

    public static VertxDataFetcher deleteTopic(AdminClientProvider acp) {
        VertxDataFetcher<String> dataFetcher = new VertxDataFetcher<>((environment, prom) -> {
            RoutingContextWrapper routingContext = environment.getContext();
            routingContext.request().headers().get("token");

            Promise deleteTopicPromise = Promise.promise();
            String topicToDelete = environment.getArgument("name");
            acp.deleteTopic(Collections.singletonList(topicToDelete), deleteTopicPromise);
            List<Types.TopicOnlyName> list = new ArrayList<>();
            deleteTopicPromise.future().onComplete(topics -> {
                prom.complete(topicToDelete);
            });
        });
        return dataFetcher;
    }
}
