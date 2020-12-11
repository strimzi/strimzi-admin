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
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class TopicDescriptionHandler extends CommonHandler {
    protected static final Logger log = LogManager.getLogger(TopicDescriptionHandler.class);

    public static VertxDataFetcher topicDescriptionFetch(Map<String, Object> acConfig, Vertx vertx) {
        VertxDataFetcher<Types.Topic> dataFetcher = new VertxDataFetcher<>((environment, prom) -> {
            RoutingContext rc = environment.getContext();
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
                log.error(e);
                if (acw != null) {
                    acw.close();
                }
                prom.fail(e);
                return;
            }

            String topicToDescribe = environment.getArgument("name");
            if (topicToDescribe == null || topicToDescribe.isEmpty()) {
                prom.fail("Topic to describe has not been specified");
            }
            Promise<Map<String, io.vertx.kafka.admin.TopicDescription>> describeTopicsPromise = Promise.promise();
            acw.describeTopics(Collections.singletonList(topicToDescribe), result -> {
                if (result.failed()) {
                    describeTopicsPromise.fail(result.cause());
                    prom.fail(result.cause());
                }
                describeTopicsPromise.complete(result.result());
            });

            Promise<Map<ConfigResource, Config>> describeTopicConfigPromise = Promise.promise();

            describeTopicsPromise.future().onFailure(
                fail -> {
                    log.error(fail);
                    prom.fail(fail);
                    return;
                }).<Types.Topic>compose(topics -> {
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

                        partition.setPartition(part.getPartition());
                        partition.setLeader(leader);
                        partition.setReplicas(replicas);
                        partition.setIsr(inSyncReplicas);
                        partitions.add(partition);
                    });
                    topic.setPartitions(partitions);
                    return Future.succeededFuture(topic);
                }).onComplete(topic -> {
                    Types.Topic t = topic.result();

                    ConfigResource resource = new ConfigResource(org.apache.kafka.common.config.ConfigResource.Type.TOPIC, topicToDescribe);
                    acw.describeConfigs(Collections.singletonList(resource), describeTopicConfigPromise);
                    describeTopicConfigPromise.future().onComplete(topics -> {
                        Config cfg = topics.result().get(resource);
                        List<ConfigEntry> entries = cfg.getEntries();

                        List<Types.ConfigEntry> topicConfigEntries = new ArrayList<>();
                        entries.stream().forEach(entry -> {
                            Types.ConfigEntry ce = new Types.ConfigEntry();
                            ce.setKey(entry.getName());
                            ce.setValue(entry.getValue());
                            topicConfigEntries.add(ce);
                        });
                        t.setConfig(topicConfigEntries);
                        prom.complete(t);
                        acw.close();
                    });
                });
        });
        return dataFetcher;
    }
}
