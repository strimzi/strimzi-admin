/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package io.strimzi.admin.common.data.fetchers;

import io.strimzi.admin.common.data.fetchers.model.Types;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.kafka.admin.Config;
import io.vertx.kafka.admin.ConfigEntry;
import io.vertx.kafka.admin.NewTopic;
import io.vertx.kafka.admin.TopicDescription;
import io.vertx.kafka.client.common.ConfigResource;
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


    public static void createTopic(AdminClientWrapper acw, Promise prom, Types.NewTopic inputTopic) {
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

        Promise createTopicPromise = Promise.promise();
        acw.createTopic(Collections.singletonList(newKafkaTopic), res -> {
            if (res.failed()) {
                prom.fail(res.cause());
                acw.close();
            } else {
                createTopicPromise.complete(res);
            }
        });

        createTopicPromise.future().onComplete(ignore -> {
            Types.Topic topic = new Types.Topic();
            List<Types.ConfigEntry> newConf = new ArrayList<>();
            inputTopic.getConfig().forEach(in -> {
                Types.ConfigEntry configEntry = new Types.ConfigEntry();
                configEntry.setKey(in.getKey());
                configEntry.setValue(in.getValue());
                newConf.add(configEntry);
            });

            topic.setConfig(newConf);
            topic.setName(inputTopic.getName());
            prom.complete(topic);
            acw.close();
        });
    }

    public static void describeTopic(AdminClientWrapper acw, Promise prom, String topicToDescribe) {
        Promise<Types.Topic> describeTopicConfigAndDescPromise = getTopicDescAndConf(acw, topicToDescribe);
        describeTopicConfigAndDescPromise.future()
            .onComplete(updated -> {
                prom.complete(updated.result());
                acw.close();
            });
    }

    private static Promise<Types.Topic> getTopicDescAndConf(AdminClientWrapper acw, String topicToDescribe) {
        Promise result = Promise.promise();
        Promise<Map<String, io.vertx.kafka.admin.TopicDescription>> describeTopicsPromise = Promise.promise();
        Promise<Map<ConfigResource, Config>> describeTopicConfigPromise = Promise.promise();

        acw.describeTopics(Collections.singletonList(topicToDescribe), describeTopicsPromise);

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
                acw.describeConfigs(Collections.singletonList(resource), describeTopicConfigPromise);
                describeTopicConfigPromise.future().onComplete(topics -> {
                    Config cfg = topics.result().get(resource);
                    t.setConfig(getTopicConf(cfg));
                    result.complete(t);
                });
            });
        return result;
    }

    public static void getList(AdminClientWrapper acw, Promise prom, Pattern pattern) {
        Promise<Set<String>> describeTopicsNamesPromise = Promise.promise();
        Promise<Map<String, io.vertx.kafka.admin.TopicDescription>> describeTopicsPromise = Promise.promise();
        Promise<Map<ConfigResource, Config>> describeTopicConfigPromise = Promise.promise();

        List<Types.Topic> partialTopicDescriptions = new ArrayList();

        acw.listTopics(describeTopicsNamesPromise);
        describeTopicsNamesPromise.future().onFailure(
            fail -> {
                log.error(fail);
                prom.fail(fail);
                acw.close();
                return;
            })
            .compose(topics -> {
                List<String> filteredList = topics.stream().filter(topicName -> byTopicName(pattern, prom).test(topicName)).collect(Collectors.toList());
                acw.describeTopics(filteredList, result -> {
                    if (result.failed()) {
                        describeTopicsPromise.fail(result.cause());
                        prom.fail(result.cause());
                    }
                    describeTopicsPromise.complete(result.result());

                });
                return describeTopicsPromise.future();
            }).<List<Types.Topic>>compose(topics -> {
                topics.entrySet().forEach(topicDesc -> {
                    partialTopicDescriptions.add(getTopicDesc(topicDesc.getValue()));
                });
                return Future.succeededFuture(partialTopicDescriptions);
            }).onComplete(descriptions -> {
                List<ConfigResource> configResourceList = new ArrayList<>();
                descriptions.result().stream().forEach(topicWithDescription -> {
                    Types.Topic t = topicWithDescription;
                    ConfigResource resource = new ConfigResource(org.apache.kafka.common.config.ConfigResource.Type.TOPIC, t.getName());
                    configResourceList.add(resource);
                });

                // cannot use getTopicDescAndConf because we need to get list
                acw.describeConfigs(configResourceList, describeTopicConfigPromise);
                describeTopicConfigPromise.future().onComplete(topicsConfigurations -> {
                    List<Types.Topic> fullTopicDescriptions = new ArrayList<>();
                    descriptions.result().forEach(topicWithDescription -> {
                        ConfigResource resource = new ConfigResource(org.apache.kafka.common.config.ConfigResource.Type.TOPIC, topicWithDescription.getName());
                        Config cfg = topicsConfigurations.result().get(resource);
                        topicWithDescription.setConfig(getTopicConf(cfg));
                        fullTopicDescriptions.add(topicWithDescription);
                    });
                    Types.TopicList topicList = new Types.TopicList();
                    topicList.setItems(fullTopicDescriptions);
                    topicList.setCount(fullTopicDescriptions.size());
                    prom.complete(topicList);
                    acw.close();
                });
            });
    }

    public static void deleteTopics(AdminClientWrapper acw, List<String> topicsToDelete, Promise prom) {
        acw.deleteTopics(topicsToDelete, res -> {
            if (res.failed()) {
                log.error(res.cause());
                prom.fail(res.cause());
                acw.close();
            } else {
                prom.complete(topicsToDelete);
                acw.close();
            }
        });
    }

    public static void updateTopic(AdminClientWrapper acw, Types.UpdatedTopic topicToUpdate, Promise prom) {

        List<ConfigEntry> ceList = new ArrayList<>();
        topicToUpdate.getConfig().stream().forEach(cfgEntry -> {
            ConfigEntry ce = new ConfigEntry(cfgEntry.getKey(), cfgEntry.getValue());
            ceList.add(ce);
        });
        Config cfg = new Config(ceList);

        ConfigResource resource = new ConfigResource(org.apache.kafka.common.config.ConfigResource.Type.TOPIC, topicToUpdate.getName());
        Promise<Void> updateTopicPromise = Promise.promise();
        acw.updateTopics(Collections.singletonMap(resource, cfg), result -> {
            if (result.failed()) {
                prom.fail(result.cause());
                acw.close();
            }
            updateTopicPromise.complete(result.result());
        });

        Promise<Types.Topic> describeTopicConfigAndDescPromise = getTopicDescAndConf(acw, topicToUpdate.getName());

        updateTopicPromise.future()
                .compose(i -> describeTopicConfigAndDescPromise.future())
                .onComplete(updated -> {
                    prom.complete(updated.result());
                    acw.close();
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
