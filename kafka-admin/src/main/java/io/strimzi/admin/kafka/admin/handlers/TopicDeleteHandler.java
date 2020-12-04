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

import java.util.Collections;

public class TopicDeleteHandler {

    public static VertxDataFetcher deleteTopic(AdminClientProvider acp) {
        VertxDataFetcher<Types.TopicOnlyName> dataFetcher = new VertxDataFetcher<>((environment, prom) -> {
            RoutingContextWrapper routingContext = environment.getContext();
            routingContext.request().headers().get("token");

            Promise deleteTopicPromise = Promise.promise();
            String topicToDelete = environment.getArgument("name");
            acp.deleteTopic(Collections.singletonList(topicToDelete), deleteTopicPromise);
            deleteTopicPromise.future().onComplete(topics -> {
                Types.TopicOnlyName topicOnlyName = new Types.TopicOnlyName();
                topicOnlyName.setName(topicToDelete);
                prom.complete(topicOnlyName);
            });
        });
        return dataFetcher;
    }
}
