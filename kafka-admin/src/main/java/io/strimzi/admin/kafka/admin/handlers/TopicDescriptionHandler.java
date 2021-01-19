/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package io.strimzi.admin.kafka.admin.handlers;

import io.strimzi.admin.kafka.admin.AdminClientWrapper;
import io.strimzi.admin.kafka.admin.TopicOperations;
import io.strimzi.admin.kafka.admin.model.Types;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.graphql.VertxDataFetcher;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;

public class TopicDescriptionHandler extends CommonHandler {
    protected static final Logger log = LogManager.getLogger(TopicDescriptionHandler.class);

    public static VertxDataFetcher topicDescriptionFetcher(Map<String, Object> acConfig, Vertx vertx) {
        VertxDataFetcher<Types.Topic> dataFetcher = new VertxDataFetcher<>((environment, prom) -> {
            setOAuthToken(acConfig, environment.getContext());
            Future<AdminClientWrapper> acw = createAdminClient(vertx, acConfig);

            String topicToDescribe = environment.getArgument("name");
            if (topicToDescribe == null || topicToDescribe.isEmpty()) {
                prom.fail("Topic to describe has not been specified");
            }

            TopicOperations.describeTopic(acw, prom, topicToDescribe);
        });
        return dataFetcher;
    }

    public static Handler<RoutingContext> topicDescriptionHandle(Map<String, Object> acConfig, Vertx vertx) {
        return routingContext -> {
            setOAuthToken(acConfig, routingContext);
            Future<AdminClientWrapper> acw = createAdminClient(vertx, acConfig);
            String topicToDescribe = routingContext.queryParams().get("name");
            Promise<Types.Topic> prom = Promise.promise();
            if (topicToDescribe == null || topicToDescribe.isEmpty()) {
                prom.fail("Topic to describe has not been specified");
            }
            TopicOperations.describeTopic(acw, prom, topicToDescribe);
            processResponse(prom, routingContext);
        };
    }
}
