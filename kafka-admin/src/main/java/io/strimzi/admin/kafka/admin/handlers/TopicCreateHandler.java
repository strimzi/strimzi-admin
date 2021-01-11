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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TopicCreateHandler extends CommonHandler {
    protected static final Logger log = LogManager.getLogger(TopicCreateHandler.class);

    public static VertxDataFetcher createTopic(Map<String, Object> acConfig, Vertx vertx) {
        VertxDataFetcher<Types.Topic> dataFetcher = new VertxDataFetcher<>((environment, prom) -> {
            setOAuthToken(acConfig, environment);
            AdminClientWrapper acw = createAdminClient(vertx, acConfig, prom);

            Types.NewTopic inputTopic = new Types.NewTopic();

            Map<String, Object> input = environment.getArgument("input");
            List<Map<String, Object>> inputConfig = (List<Map<String, Object>>) input.get("config");
            List<Types.NewTopicConfigEntry> newTopicConfigEntries = new ArrayList<>();

            inputConfig.forEach(entry -> {
                Types.NewTopicConfigEntry newTopicConfigEntry = new Types.NewTopicConfigEntry();
                newTopicConfigEntry.setKey(entry.get("key").toString());
                newTopicConfigEntry.setValue(entry.get("value").toString());
                newTopicConfigEntries.add(newTopicConfigEntry);
            });

            inputTopic.setConfig(newTopicConfigEntries);
            inputTopic.setName(input.get("name").toString());
            inputTopic.setNumPartitions(Integer.parseInt(input.get("numPartitions").toString()));
            inputTopic.setReplicationFactor(Integer.parseInt(input.get("replicationFactor").toString()));

            TopicOperations.createTopic(acw, prom, inputTopic);
        });
        return dataFetcher;
    }
}
