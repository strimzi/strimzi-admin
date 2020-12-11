/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package io.strimzi.admin.kafka.admin.handlers;

import io.strimzi.admin.kafka.admin.AdminClientWrapper;
import io.strimzi.admin.kafka.admin.model.Types;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.graphql.VertxDataFetcher;
import io.vertx.kafka.admin.NewTopic;
import org.apache.kafka.common.config.SaslConfigs;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TopicCreateHandler extends CommonHandler {
    protected static final Logger log = LogManager.getLogger(TopicCreateHandler.class);

    public static VertxDataFetcher createTopic(Map<String, Object> acConfig, Vertx vertx) {
        VertxDataFetcher<Types.Topic> dataFetcher = new VertxDataFetcher<>((environment, prom) -> {
            RoutingContext rc = environment.getContext();
            String token = rc.request().getHeader("Authorization");
            if (token != null) {
                if (token.startsWith("Bearer ")) {
                    token = token.substring("Bearer ".length());
                }
                log.info("auth token is {}", token);
                log.info(SaslConfigs.SASL_JAAS_CONFIG + "is org.apache.kafka.common.security.oauthbearer.OAuthBearerLoginModule required oauth.access.token=\"" + token + " \";");
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

            NewTopic newKafkaTopic = new NewTopic();
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
                    log.error(res.cause());
                    prom.fail(res.cause());
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
        });
        return dataFetcher;
    }
}
