/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package io.strimzi.admin.kafka.admin.handlers;

import io.strimzi.admin.common.data.fetchers.AdminClientWrapper;
import io.strimzi.admin.common.data.fetchers.TopicOperations;
import io.vertx.core.Vertx;
import io.vertx.ext.web.handler.graphql.VertxDataFetcher;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Map;

public class TopicsDeleteHandler extends CommonHandler {
    protected static final Logger log = LogManager.getLogger(TopicsDeleteHandler.class);

    public static VertxDataFetcher deleteTopics(Map<String, Object> acConfig, Vertx vertx) {
        VertxDataFetcher<List<String>> dataFetcher = new VertxDataFetcher<>((environment, prom) -> {
            setOAuthToken(acConfig, environment);
            AdminClientWrapper acw = createAdminClient(vertx, acConfig, prom);

            List<String> topicsToDelete = environment.getArgument("names");
            TopicOperations.deleteTopics(acw, topicsToDelete, prom);
        });
        return dataFetcher;
    }
}
