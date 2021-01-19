/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package io.strimzi.admin.kafka.admin.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.handler.codec.http.HttpResponseStatus;
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TopicUpdateHandler extends CommonHandler {
    protected static final Logger log = LogManager.getLogger(TopicUpdateHandler.class);

    public static VertxDataFetcher updateTopicFetcher(Map<String, Object> acConfig, Vertx vertx) {
        VertxDataFetcher<Types.Topic> dataFetcher = new VertxDataFetcher<>((environment, prom) -> {
            setOAuthToken(acConfig, environment.getContext());
            Future<AdminClientWrapper> acw = createAdminClient(vertx, acConfig);

            Types.UpdatedTopic updatedTopic = new Types.UpdatedTopic();

            Map<String, Object> input = environment.getArgument("input");
            List<Map<String, Object>> inputConfig = (List<Map<String, Object>>) input.get("config");
            List<Types.NewTopicConfigEntry> newTopicConfigEntries = new ArrayList<>();

            inputConfig.forEach(entry -> {
                Types.NewTopicConfigEntry newTopicConfigEntry = new Types.NewTopicConfigEntry();
                newTopicConfigEntry.setKey(entry.get("key").toString());
                newTopicConfigEntry.setValue(entry.get("value").toString());
                newTopicConfigEntries.add(newTopicConfigEntry);
            });

            updatedTopic.setConfig(newTopicConfigEntries);
            updatedTopic.setName(input.get("name").toString());

            TopicOperations.updateTopic(acw, updatedTopic, prom);
        });
        return dataFetcher;
    }

    public static Handler<RoutingContext> updateTopicHandler(Map<String, Object> acConfig, Vertx vertx) {
        return routingContext -> {
            setOAuthToken(acConfig, routingContext);
            Future<AdminClientWrapper> acw = createAdminClient(vertx, acConfig);
            Types.UpdatedTopic updatedTopic = new Types.UpdatedTopic();
            Promise<Types.UpdatedTopic> prom = Promise.promise();
            ObjectMapper mapper = new ObjectMapper();
            try {
                updatedTopic = mapper.readValue(routingContext.getBody().getBytes(), Types.UpdatedTopic.class);
            } catch (IOException e) {
                routingContext.response().setStatusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.code());
                routingContext.response().end(e.getMessage());
                prom.fail(e);
            }

            TopicOperations.updateTopic(acw, updatedTopic, prom);
            processResponse(prom, routingContext);
        };
    }
}
