/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package io.strimzi.admin.kafka.admin.handlers;

import io.strimzi.admin.common.data.fetchers.AdminClientWrapper;
import io.strimzi.admin.common.data.fetchers.TopicOperations;
import io.strimzi.admin.common.data.fetchers.model.Types;
import io.vertx.core.Vertx;
import io.vertx.ext.web.handler.graphql.VertxDataFetcher;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;
import java.util.regex.Pattern;

public class TopicListHandler extends CommonHandler {
    protected static final Logger log = LogManager.getLogger(TopicListHandler.class);

    public static VertxDataFetcher topicListFetch(Map<String, Object> acConfig, Vertx vertx) {
        VertxDataFetcher<Types.TopicList> dataFetcher = new VertxDataFetcher<>((env, prom) -> {
            setOAuthToken(acConfig, env);
            AdminClientWrapper acw = createAdminClient(vertx, acConfig, prom);

            String argument = env.getArgument("search");
            final Pattern pattern;
            if (argument != null && !argument.isEmpty()) {
                pattern = Pattern.compile(argument);
            } else {
                pattern = null;
            }

            TopicOperations.getList(acw, prom, pattern);
        });
        return dataFetcher;
    }
}
