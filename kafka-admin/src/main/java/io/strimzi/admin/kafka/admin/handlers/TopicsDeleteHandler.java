/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package io.strimzi.admin.kafka.admin.handlers;

import io.strimzi.admin.kafka.admin.AdminClientWrapper;
import io.vertx.ext.web.handler.graphql.VertxDataFetcher;
import java.util.List;

public class TopicsDeleteHandler {

    public static VertxDataFetcher deleteTopics(AdminClientWrapper acw) {
        VertxDataFetcher<List<String>> dataFetcher = new VertxDataFetcher<>((environment, prom) -> {
            List<String> topicsToDelete = environment.getArgument("names");
            acw.deleteTopics(topicsToDelete, res -> {
                if (res.failed()) {
                    prom.fail(res.cause());
                } else {
                    prom.complete(topicsToDelete);
                }
            });
        });
        return dataFetcher;
    }
}
