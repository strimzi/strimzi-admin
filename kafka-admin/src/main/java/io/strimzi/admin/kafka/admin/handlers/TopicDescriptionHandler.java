/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package io.strimzi.admin.kafka.admin.handlers;

import io.strimzi.admin.kafka.admin.AdminClientProvider;
import io.strimzi.admin.kafka.admin.model.Types;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.ext.web.handler.graphql.VertxDataFetcher;
import io.vertx.kafka.admin.Config;
import io.vertx.kafka.admin.ConfigEntry;
import io.vertx.kafka.client.common.ConfigResource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class TopicDescriptionHandler {

    public static VertxDataFetcher topicDescriptionFetch(AdminClientProvider acp) {
        VertxDataFetcher<Types.Topic> dataFetcher = new VertxDataFetcher<>((environment, prom) -> {
            String topicToDescribe = environment.getArgument("name");
            if (topicToDescribe == null || topicToDescribe.isEmpty()) {
                prom.fail("Topic to describe has not been specified");
            }
            Promise<Map<String, io.vertx.kafka.admin.TopicDescription>> describeTopicsPromise = Promise.promise();
            acp.describeTopics(Collections.singletonList(topicToDescribe), describeTopicsPromise);

            ConfigResource resource = new ConfigResource(org.apache.kafka.common.config.ConfigResource.Type.TOPIC, topicToDescribe);
            Promise<Map<ConfigResource, Config>> describeTopicConfigPromise = Promise.promise();

            describeTopicsPromise.future().<Types.Topic>compose(topics -> {
                io.vertx.kafka.admin.TopicDescription topicDesc = topics.get(topicToDescribe);
                Types.Topic topic = new Types.Topic();
                topic.setName(topicDesc.getName());
                topic.setIsInternal(topicDesc.isInternal());
                List<Types.Partition> partitions = new ArrayList<>();
                topicDesc.getPartitions().forEach(part -> {
                    Types.Partition partition = new Types.Partition();
                    Types.Node leader = new Types.Node();
                    leader.setId(part.getLeader().getId());

                    List<Types.Node> replicas = new ArrayList<>();
                    part.getReplicas().forEach(rep -> {
                        Types.Node replica = new Types.Node();
                        replica.setId(rep.getId());
                        replicas.add(replica);
                    });

                    List<Types.Node> inSyncReplicas = new ArrayList<>();
                    part.getIsr().forEach(isr -> {
                        Types.Node inSyncReplica = new Types.Node();
                        inSyncReplica.setId(isr.getId());
                        inSyncReplicas.add(inSyncReplica);
                    });

                    partition.setPartition(partition.getPartition());
                    partition.setLeader(leader);
                    partition.setReplicas(replicas);
                    partition.setIsr(inSyncReplicas);
                    partitions.add(partition);
                });
                topic.setPartitions(partitions);
                return Future.succeededFuture(topic);
            }).onComplete(topic -> {
                Types.Topic t = topic.result();

                acp.describeConfigs(Collections.singletonList(resource), describeTopicConfigPromise);
                describeTopicConfigPromise.future().onComplete(topics -> {
                    Config cfg = topics.result().get(resource);
                    List<ConfigEntry> entries = cfg.getEntries();

                    List<Types.ConfigEntry> topicConfigEntries = new ArrayList<>();
                    // way2 put there everything
                    entries.stream().forEach(entry -> {
                        Types.ConfigEntry ce = new Types.ConfigEntry();
                        ce.setKey(entry.getName());
                        ce.setValue(entry.getValue());
                        topicConfigEntries.add(ce);
                    });
                    t.setConfig(topicConfigEntries);
                    prom.complete(t);
                });
            });
        });
        return dataFetcher;
    }
}
