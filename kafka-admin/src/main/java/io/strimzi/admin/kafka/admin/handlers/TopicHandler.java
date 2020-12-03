package io.strimzi.admin.kafka.admin.handlers;

import io.strimzi.admin.kafka.admin.AdminClientProvider;
import io.strimzi.admin.kafka.admin.model.Types;
import io.vertx.core.Promise;
import io.vertx.ext.web.handler.graphql.VertxDataFetcher;
import io.vertx.kafka.admin.TopicDescription;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class TopicHandler {

    public static VertxDataFetcher topicInfoFetch(AdminClientProvider acp) {
        VertxDataFetcher<Types.Topic> dataFetcher = new VertxDataFetcher<>((environment, prom)-> {
            String topicToDescribe = environment.getArgument("name");
            if (topicToDescribe == null || topicToDescribe.isEmpty()) {
                prom.fail("Topic to describe has not been specified");
            }
            Promise<Map<String, TopicDescription>> describeTopicsPromise = Promise.promise();
            acp.describeTopics(Collections.singletonList(topicToDescribe), describeTopicsPromise);
            describeTopicsPromise.future().onComplete(topics -> {
                TopicDescription topicDesc = topics.result().get(topicToDescribe);
                Types.Topic topic = new Types.Topic();
                topic.setName(topicDesc.getName());
                Types.TopicConfig tc = new Types.TopicConfig();
                tc.setIsInternal(topicDesc.isInternal());
                tc.setpartitionCount(topicDesc.getPartitions().stream().count());
                tc.setReplicationFactor(topicDesc.getPartitions().get(0).getReplicas().stream().count());
                
                List<Types.Partitions> partitionsList = new ArrayList<>();
                topicDesc.getPartitions().forEach(part -> {
                    Types.Partitions partition = new Types.Partitions();
                    partition.setPartition(part.getPartition());
                    List<Types.Replicas> replicasList = new ArrayList<>();
                    part.getReplicas().forEach(replica -> {
                        Types.Replicas replicas = new Types.Replicas();
                        replicas.setId(replica.getIdString());
                        replicas.setIn_sync(replica.isEmpty());
                        replicasList.add(replicas);
                    });
                    partition.setReplicas(replicasList);
                    partitionsList.add(partition);
                });
                tc.setPartitions(partitionsList);
                topic.setConfig(tc);
                prom.complete(topic);
            });
        });
        return dataFetcher;
    }
}
