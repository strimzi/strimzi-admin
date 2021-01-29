/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package io.strimzi.admin.kafka.admin;

import io.strimzi.admin.kafka.admin.handlers.CommonHandler;
import io.strimzi.admin.kafka.admin.model.Types;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.kafka.admin.Config;
import io.vertx.kafka.admin.ConfigEntry;
import io.vertx.kafka.admin.KafkaAdminClient;
import io.vertx.kafka.admin.NewTopic;
import io.vertx.kafka.admin.TopicDescription;
import io.vertx.kafka.client.common.ConfigResource;
import org.apache.kafka.common.errors.InvalidRequestException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;

public class TopicOperations {
    protected static final Logger log = LogManager.getLogger(TopicOperations.class);


    public static void createTopic(KafkaAdminClient ac, Promise prom, Types.NewTopic inputTopic) {
        NewTopic newKafkaTopic = new NewTopic();

        Map<String, String> config = new HashMap<>();
        List<Types.NewTopicConfigEntry> configObject = inputTopic.getConfig();
        configObject.forEach(item -> {
            config.put(item.getKey(), item.getValue());
        });

        newKafkaTopic.setName(inputTopic.getName());
        newKafkaTopic.setReplicationFactor(inputTopic.getReplicationFactor().shortValue());
        newKafkaTopic.setNumPartitions(inputTopic.getNumPartitions());
        if (config != null) {
            newKafkaTopic.setConfig(config);
        }

        ac.createTopics(Collections.singletonList(newKafkaTopic), res -> {
            if (res.failed()) {
                prom.fail(res.cause());
                ac.close();
            } else {
                getTopicDescAndConf(ac, inputTopic.getName()).future()
                    .onComplete(desc -> {
                        if (desc.failed()) {
                            prom.fail(desc.cause());
                        } else {
                            prom.complete(desc.result());
                        }
                        ac.close();
                    });
            }
        });

    }

    public static void describeTopic(KafkaAdminClient ac, Promise prom, String topicToDescribe) {
        Promise<Types.Topic> describeTopicConfigAndDescPromise = getTopicDescAndConf(ac, topicToDescribe);
        describeTopicConfigAndDescPromise.future()
            .onComplete(description -> {
                if (description.failed()) {
                    prom.fail(description.cause());
                } else {
                    prom.complete(description.result());
                }
                ac.close();
            });
    }

    private static Promise<Types.Topic> getTopicDescAndConf(KafkaAdminClient ac, String topicToDescribe) {
        Promise result = Promise.promise();
        Promise<Map<String, io.vertx.kafka.admin.TopicDescription>> describeTopicsPromise = Promise.promise();
        Promise<Map<ConfigResource, Config>> describeTopicConfigPromise = Promise.promise();

        ac.describeTopics(Collections.singletonList(topicToDescribe), describeTopicsPromise);

        describeTopicsPromise.future().onFailure(
            fail -> {
                log.error(fail);
                result.fail(fail);
                return;
            }).<Types.Topic>compose(topics -> {
                io.vertx.kafka.admin.TopicDescription topicDesc = topics.get(topicToDescribe);
                return Future.succeededFuture(getTopicDesc(topicDesc));
            }).onComplete(topic -> {
                Types.Topic t = topic.result();
                ConfigResource resource = new ConfigResource(org.apache.kafka.common.config.ConfigResource.Type.TOPIC, topicToDescribe);
                ac.describeConfigs(Collections.singletonList(resource), describeTopicConfigPromise);
                describeTopicConfigPromise.future().onComplete(topics -> {
                    if (topics.failed()) {
                        result.fail(topics.cause());
                    } else {
                        Config cfg = topics.result().get(resource);
                        t.setConfig(getTopicConf(cfg));
                        result.complete(t);
                    }
                });
            });
        return result;
    }

    public static void getList(KafkaAdminClient ac, Promise prom, Pattern pattern, int offset, final int limit) {
        Promise<Set<String>> describeTopicsNamesPromise = Promise.promise();
        Promise<Map<String, io.vertx.kafka.admin.TopicDescription>> describeTopicsPromise = Promise.promise();
        Promise<Map<ConfigResource, Config>> describeTopicConfigPromise = Promise.promise();

        List<ConfigResource> configResourceList = new ArrayList<>();
        List<Types.Topic> fullDescription = new ArrayList<>();

        ac.listTopics(describeTopicsNamesPromise);
        describeTopicsNamesPromise.future()
            .compose(topics -> {
                List<String> filteredList = topics.stream().filter(topicName -> byTopicName(pattern, prom).test(topicName)).collect(Collectors.toList());
                ac.describeTopics(filteredList, describeTopicsPromise);
                return describeTopicsPromise.future();
            }).compose(topics -> {
                topics.entrySet().forEach(topicWithDescription -> {
                    Types.Topic desc = getTopicDesc(topicWithDescription.getValue());
                    fullDescription.add(desc);
                    ConfigResource resource = new ConfigResource(org.apache.kafka.common.config.ConfigResource.Type.TOPIC, desc.getName());
                    configResourceList.add(resource);
                });
                ac.describeConfigs(configResourceList, describeTopicConfigPromise);
                return describeTopicConfigPromise.future();
            }).compose(topicsConfigurations -> {
                List<Types.Topic> fullTopicDescriptions = new ArrayList<>();
                fullDescription.forEach(topicWithDescription -> {
                    ConfigResource resource = new ConfigResource(org.apache.kafka.common.config.ConfigResource.Type.TOPIC, topicWithDescription.getName());
                    Config cfg = topicsConfigurations.get(resource);
                    topicWithDescription.setConfig(getTopicConf(cfg));
                    fullTopicDescriptions.add(topicWithDescription);
                });
                Types.TopicList topicList = new Types.TopicList();
                //topicList.setItems(fullTopicDescriptions.stream().sorted().collect(Collectors.toList()).subList(offset, limit));
                fullTopicDescriptions.sort(new CommonHandler.TopicComparator());

                if (offset > fullTopicDescriptions.size()) {
                    return Future.failedFuture(new InvalidRequestException("Offset (" + offset + ") cannot be greater than topic list size (" + fullTopicDescriptions.size() + ")"));
                }
                int tmpLimit = limit;
                if (tmpLimit == 0) {
                    tmpLimit = fullTopicDescriptions.size();
                }

                List<Types.Topic> croppedList = fullTopicDescriptions.subList(offset, Math.min(offset + tmpLimit, fullTopicDescriptions.size()));
                topicList.setItems(croppedList);
                topicList.setCount(croppedList.size()); // TODO do we want to return full list count or filtered?
                topicList.setLimit(tmpLimit);
                topicList.setOffset(offset);
                return Future.succeededFuture(topicList);
            }).onComplete(finalRes -> {
                if (finalRes.failed()) {
                    prom.fail(finalRes.cause());
                } else {
                    prom.complete(finalRes.result());
                }
                ac.close();
            });
    }

    public static void deleteTopics(KafkaAdminClient ac, List<String> topicsToDelete, Promise prom) {
        ac.deleteTopics(topicsToDelete, res -> {
            if (res.failed()) {
                log.error(res.cause());
                prom.fail(res.cause());
            } else {
                prom.complete(topicsToDelete);
            }
            ac.close();
        });
    }

    public static void updateTopic(KafkaAdminClient ac, Types.UpdatedTopic topicToUpdate, Promise prom) {
        List<ConfigEntry> ceList = new ArrayList<>();
        topicToUpdate.getConfig().stream().forEach(cfgEntry -> {
            ConfigEntry ce = new ConfigEntry(cfgEntry.getKey(), cfgEntry.getValue());
            ceList.add(ce);
        });
        Config cfg = new Config(ceList);

        ConfigResource resource = new ConfigResource(org.apache.kafka.common.config.ConfigResource.Type.TOPIC, topicToUpdate.getName());
        Promise<Void> updateTopicConfigPromise = Promise.promise();

        // we have to describe first, otherwise we cannot determine whether the topic exists or not (alterConfigs returns just server error)
        getTopicDescAndConf(ac, topicToUpdate.getName()).future()
                .compose(a -> {
                    ac.alterConfigs(Collections.singletonMap(resource, cfg), updateTopicConfigPromise);
                    return updateTopicConfigPromise.future();
                })
                .compose(update -> getTopicDescAndConf(ac, topicToUpdate.getName()).future())
                .onComplete(desc -> {
                    if (desc.failed()) {
                        prom.fail(desc.cause());
                    } else {
                        prom.complete(desc.result());
                    }
                    ac.close();
                });
    }

    private static Predicate<String> byTopicName(Pattern pattern, Promise prom) {
        return topic -> {
            if (pattern == null) {
                return true;
            } else {
                try {
                    Matcher matcher = pattern.matcher(topic);
                    return matcher.find();
                } catch (PatternSyntaxException ex) {
                    prom.fail(ex);
                    return false;
                }
            }
        };
    }

    private static List<Types.ConfigEntry> getTopicConf(Config cfg) {
        List<ConfigEntry> entries = cfg.getEntries();
        List<Types.ConfigEntry> topicConfigEntries = new ArrayList<>();
        entries.stream().forEach(entry -> {
            Types.ConfigEntry ce = new Types.ConfigEntry();
            ce.setKey(entry.getName());
            ce.setValue(entry.getValue());
            topicConfigEntries.add(ce);
        });
        return topicConfigEntries;
    }

    /**
     * @param topicDesc topic to describe
     * @returntopic description without configuration
     */
    private static Types.Topic getTopicDesc(TopicDescription topicDesc) {
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

            partition.setPartition(part.getPartition());
            partition.setLeader(leader);
            partition.setReplicas(replicas);
            partition.setIsr(inSyncReplicas);
            partitions.add(partition);
        });
        topic.setPartitions(partitions);

        return topic;
    }
}
