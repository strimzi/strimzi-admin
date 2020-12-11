/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package io.strimzi.admin.kafka.admin.handlers;

import io.strimzi.admin.kafka.admin.AdminClientWrapper;
import io.strimzi.admin.kafka.admin.model.Types;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.graphql.VertxDataFetcher;
import io.vertx.kafka.admin.Config;
import io.vertx.kafka.admin.ConfigEntry;
import io.vertx.kafka.client.common.ConfigResource;
import org.apache.kafka.common.config.SaslConfigs;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;

public class TopicListHandler extends CommonHandler {
    protected static final Logger log = LogManager.getLogger(TopicListHandler.class);

    public static VertxDataFetcher topicListFetch(Map<String, Object> acConfig, Vertx vertx) {
        VertxDataFetcher<Types.TopicList> dataFetcher = new VertxDataFetcher<>((env, prom) -> {
            RoutingContext rc = env.getContext();
            String token = rc.request().getHeader("Authorization");
            if (token != null) {
                if (token.startsWith("Bearer ")) {
                    token = token.substring("Bearer ".length());
                }
                log.info("auth token is {}", token);
                log.info(SaslConfigs.SASL_JAAS_CONFIG + "is org.apache.kafka.common.security.oauthbearer.OAuthBearerLoginModule required oauth.access.token=\"" + token + "\";");
                acConfig.put(SaslConfigs.SASL_JAAS_CONFIG, "org.apache.kafka.common.security.oauthbearer.OAuthBearerLoginModule required oauth.access.token=\"" + token + "\";");
            }

            AdminClientWrapper acw = new AdminClientWrapper(vertx, acConfig);
            try {
                acw.open();
            } catch (Exception e) {
                prom.fail(e);
                log.error(e);
                return;
            }

            String argument = env.getArgument("search");
            final Pattern pattern;
            if (argument != null && !argument.isEmpty()) {
                pattern = Pattern.compile(argument);
            } else {
                pattern = null;
            }

            Promise<Set<String>> describeTopicsNamesPromise = Promise.promise();
            Promise<Map<String, io.vertx.kafka.admin.TopicDescription>> describeTopicsPromise = Promise.promise();
            Promise<Map<ConfigResource, Config>> describeTopicConfigPromise = Promise.promise();

            List<Types.Topic> partialTopicDescriptions = new ArrayList();

            acw.listTopics(describeTopicsNamesPromise);
            describeTopicsNamesPromise.future().onFailure(
                fail -> {
                    log.error(fail);
                    describeTopicsNamesPromise.fail(fail);
                    prom.fail(fail);
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
                            Types.Topic topic = new Types.Topic();
                            topic.setName(topicDesc.getValue().getName());
                            topic.setIsInternal(topicDesc.getValue().isInternal());
                            List<Types.Partition> partitions = new ArrayList<>();
                            topicDesc.getValue().getPartitions().forEach(part -> {
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
                            partialTopicDescriptions.add(topic);
                        });
                        return Future.succeededFuture(partialTopicDescriptions);
                    }).onComplete(descriptions -> {
                        List<ConfigResource> configResourceList = new ArrayList<>();
                        descriptions.result().stream().forEach(topicWithDescription -> {
                            Types.Topic t = topicWithDescription;
                            ConfigResource resource = new ConfigResource(org.apache.kafka.common.config.ConfigResource.Type.TOPIC, t.getName());
                            configResourceList.add(resource);
                        });

                        acw.describeConfigs(configResourceList, describeTopicConfigPromise);
                        describeTopicConfigPromise.future().onComplete(topicsConfigurations -> {
                            List<Types.Topic> fullTopicDescriptions = new ArrayList<>();
                            descriptions.result().forEach(topicWithDescription -> {
                                ConfigResource resource = new ConfigResource(org.apache.kafka.common.config.ConfigResource.Type.TOPIC, topicWithDescription.getName());
                                Config cfg = topicsConfigurations.result().get(resource);
                                List<ConfigEntry> entries = cfg.getEntries();

                                List<Types.ConfigEntry> topicConfigEntries = new ArrayList<>();
                                entries.stream().forEach(entry -> {
                                    Types.ConfigEntry ce = new Types.ConfigEntry();
                                    ce.setKey(entry.getName());
                                    ce.setValue(entry.getValue());
                                    topicConfigEntries.add(ce);
                                });
                                topicWithDescription.setConfig(topicConfigEntries);
                                fullTopicDescriptions.add(topicWithDescription);
                            });
                            Types.TopicList topicList = new Types.TopicList();
                            topicList.setItems(fullTopicDescriptions);
                            topicList.setCount(fullTopicDescriptions.size());
                            prom.complete(topicList);
                            acw.close();
                        });

                    });
        });
        return dataFetcher;
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
}
