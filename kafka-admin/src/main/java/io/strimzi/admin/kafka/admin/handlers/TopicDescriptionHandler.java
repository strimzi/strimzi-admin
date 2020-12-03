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
        VertxDataFetcher<Types.TopicDescription> dataFetcher = new VertxDataFetcher<>((environment, prom) -> {
            String topicToDescribe = environment.getArgument("name");
            if (topicToDescribe == null || topicToDescribe.isEmpty()) {
                prom.fail("Topic to describe has not been specified");
            }
            Promise<Map<String, io.vertx.kafka.admin.TopicDescription>> describeTopicsPromise = Promise.promise();
            acp.describeTopics(Collections.singletonList(topicToDescribe), describeTopicsPromise);

            ConfigResource resource = new ConfigResource(org.apache.kafka.common.config.ConfigResource.Type.TOPIC, topicToDescribe);
            Promise<Map<ConfigResource, Config>> describeTopicConfigPromise = Promise.promise();

            describeTopicsPromise.future().<Types.TopicDescription>compose(topics -> {
                io.vertx.kafka.admin.TopicDescription topicDesc = topics.get(topicToDescribe);
                Types.TopicDescription topicDescription = new Types.TopicDescription();
                topicDescription.setName(topicDesc.getName());
                Types.TopicConfig tc = new Types.TopicConfig();
                tc.setIsInternal(topicDesc.isInternal());
                tc.setPartitionCount(topicDesc.getPartitions().stream().count());
                tc.setReplicationFactor(topicDesc.getPartitions().get(0).getReplicas().stream().count());
                List<Types.Partitions> partitionsList = new ArrayList<>();
                topicDesc.getPartitions().forEach(part -> {
                    Types.Partitions partition = new Types.Partitions();
                    partition.setPartition(part.getPartition());
                    List<Types.Replicas> replicasList = new ArrayList<>();

                    // set all replicas in sync to false
                    part.getReplicas().forEach(replica -> {
                        Types.Replicas replicas = new Types.Replicas();
                        replicas.setId(replica.getIdString());
                        replicas.setInSync(false);
                        replicasList.add(replicas);
                    });
                    partition.setReplicas(replicasList);
                    // set all in sync replicas to true
                    part.getIsr().forEach(replica -> {
                        partition.getReplicas().get(replica.getId()).setInSync(true);
                    });
                    partitionsList.add(partition);
                });
                topicDescription.setPartitions(partitionsList);
                topicDescription.setConfig(tc);
                return Future.succeededFuture(topicDescription);
            }).onComplete(topic -> {
                Types.TopicDescription t = topic.result();
                Types.TopicConfig tc = topic.result().getConfig();

                acp.describeConfigs(Collections.singletonList(resource), describeTopicConfigPromise);
                describeTopicConfigPromise.future().onComplete(topics -> {
                    Config cfg = topics.result().get(resource);
                    List<ConfigEntry> entries = cfg.getEntries();

                    // way 1 to fill $set od properties
                    // what is the $set?
                    //tc.setRetentionMs("retentionMs", entries.stream().filter(nts -> nts.getName().equals("retention.ms")).findFirst().get().getValue());
                    tc.setMinInsyncReplicas(Long.parseLong(entries.stream().filter(nts -> nts.getName().equals("min.insync.replicas")).findFirst().get().getValue()));

                    List<Types.TopicConfigEntry> topicConfigEntries = new ArrayList<>();
                    // way2 put there everything
                    entries.stream().forEach(entry -> {
                        Types.TopicConfigEntry tce = new Types.TopicConfigEntry();
                        tce.setKey(entry.getName());
                        tce.setValue(entry.getValue());
                        topicConfigEntries.add(tce);
                    });
                    tc.setPairs(topicConfigEntries);

                    t.setConfig(tc);
                    prom.complete(t);
                });


            });
        });
        return dataFetcher;
    }
}
