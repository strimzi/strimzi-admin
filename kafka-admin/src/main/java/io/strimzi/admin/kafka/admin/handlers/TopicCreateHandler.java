/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package io.strimzi.admin.kafka.admin.handlers;

import io.strimzi.admin.kafka.admin.AdminClientProvider;
import io.strimzi.admin.kafka.admin.model.Types;
import io.vertx.core.Promise;
import io.vertx.ext.web.handler.graphql.VertxDataFetcher;
import io.vertx.kafka.admin.NewTopic;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class TopicCreateHandler {

    public static VertxDataFetcher createTopic(AdminClientProvider acp) {
        VertxDataFetcher<Types.CreateTopicInput> dataFetcher = new VertxDataFetcher<>((environment, prom) -> {

            NewTopic newTopic = new NewTopic();
            Map<String, Object> input = environment.getArgument("input");
            newTopic.setName(input.get("name").toString());
            Map<String, String> config = new HashMap<>();
            Map<String, Object> configObject = (Map<String, Object>) input.get("config");
            Types.CreateTopicInput createTopicInput = new Types.CreateTopicInput();
            createTopicInput.setName(input.get("name").toString());
            Types.CreateOrMutateTopicConfigInput createOrMutateTopicConfigInput = new Types.CreateOrMutateTopicConfigInput();
            if (configObject.get("minInSyncReplicas") != null) {
                Integer val = (Integer) configObject.get("minInSyncReplicas");
                createOrMutateTopicConfigInput.setMinInSyncReplicas(val);
            }
            if (configObject.get("partitionCount") != null) {
                String val = configObject.get("partitionCount").toString();
                createOrMutateTopicConfigInput.setPartitionCount(Long.parseLong(val));
                newTopic.setNumPartitions(Integer.parseInt(val));
            }
            if (configObject.get("replicationFactor") != null) {
                String val = configObject.get("replicationFactor").toString();
                createOrMutateTopicConfigInput.setReplicationFactor(Long.parseLong(val));
                newTopic.setReplicationFactor(Short.parseShort(val));
            }
            if (configObject.get("retentionDays") != null) {
                String val = configObject.get("retentionDays").toString();
                createOrMutateTopicConfigInput.setRetentionDays(Integer.parseInt(val));
                Integer daysToHours = Integer.parseInt(val) * 86400000;
                config.put("retention.ms", daysToHours.toString());
            }

            createTopicInput.setConfig(createOrMutateTopicConfigInput);
            newTopic.setConfig(config);
            Promise createTopicPromise = Promise.promise();
            acp.createTopic(Collections.singletonList(newTopic), createTopicPromise);
            createTopicPromise.future().onComplete(topics -> {
                prom.complete(createTopicInput);
            });
        });
        return dataFetcher;
    }
}
