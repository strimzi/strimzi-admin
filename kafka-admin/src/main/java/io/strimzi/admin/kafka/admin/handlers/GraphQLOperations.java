/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package io.strimzi.admin.kafka.admin.handlers;

import io.strimzi.admin.kafka.admin.TopicOperations;
import io.strimzi.admin.kafka.admin.model.Types;
import io.vertx.core.Vertx;
import io.vertx.ext.web.handler.graphql.VertxDataFetcher;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class GraphQLOperations extends CommonHandler implements OperationsHandler<VertxDataFetcher> {
    @Override
    public VertxDataFetcher createTopic(Map<String, Object> acConfig, Vertx vertx) {
        return new VertxDataFetcher<>((environment, prom) -> {
            setOAuthToken(acConfig, environment.getContext());
            createAdminClient(vertx, acConfig).onComplete(ac -> {
                if (ac.failed()) {
                    prom.fail(ac.cause());
                } else {

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

                    TopicOperations.createTopic(ac.result(), prom, inputTopic);
                }
            });
        });
    }

    @Override
    public VertxDataFetcher describeTopic(Map<String, Object> acConfig, Vertx vertx) {
        VertxDataFetcher<Types.Topic> dataFetcher = new VertxDataFetcher<>((environment, prom) -> {
            setOAuthToken(acConfig, environment.getContext());

            String topicToDescribe = environment.getArgument("name");
            if (topicToDescribe == null || topicToDescribe.isEmpty()) {
                prom.fail("Topic to describe has not been specified");
            }

            createAdminClient(vertx, acConfig).onComplete(ac -> {
                if (ac.failed()) {
                    prom.fail(ac.cause());
                } else {
                    TopicOperations.describeTopic(ac.result(), prom, topicToDescribe);
                }
            });
        });
        return dataFetcher;
    }

    @Override
    public VertxDataFetcher updateTopic(Map<String, Object> acConfig, Vertx vertx) {
        VertxDataFetcher<Types.Topic> dataFetcher = new VertxDataFetcher<>((environment, prom) -> {
            setOAuthToken(acConfig, environment.getContext());

            Types.UpdatedTopic updatedTopic = new Types.UpdatedTopic();

            Map<String, Object> input = environment.getArgument("input");
            List<Map<String, Object>> inputConfig = (List<Map<String, Object>>) input.get("config");
            List<Types.NewTopicConfigEntry> newTopicConfigEntries = new ArrayList<>();

            inputConfig.forEach(entry -> {
                Types.NewTopicConfigEntry newTopicConfigEntry = new Types.NewTopicConfigEntry();
                newTopicConfigEntry.setKey(entry.get("key").toString());
                newTopicConfigEntry.setValue(entry.get("value").toString());
                newTopicConfigEntries.add(newTopicConfigEntry);
            });

            updatedTopic.setConfig(newTopicConfigEntries);
            updatedTopic.setName(input.get("name").toString());

            createAdminClient(vertx, acConfig).onComplete(ac -> {
                if (ac.failed()) {
                    prom.fail(ac.cause());
                } else {
                    TopicOperations.updateTopic(ac.result(), updatedTopic, prom);
                }
            });
        });
        return dataFetcher;
    }

    @Override
    public VertxDataFetcher deleteTopic(Map<String, Object> acConfig, Vertx vertx) {
        VertxDataFetcher<List<String>> dataFetcher = new VertxDataFetcher<>((environment, prom) -> {
            setOAuthToken(acConfig, environment.getContext());

            List<String> topicsToDelete = environment.getArgument("names");
            createAdminClient(vertx, acConfig).onComplete(ac -> {
                if (ac.failed()) {
                    prom.fail(ac.cause());
                } else {
                    TopicOperations.deleteTopics(ac.result(), topicsToDelete, prom);
                }
            });
        });
        return dataFetcher;
    }

    @Override
    public VertxDataFetcher listTopics(Map<String, Object> acConfig, Vertx vertx) {
        VertxDataFetcher<Types.TopicList> dataFetcher = new VertxDataFetcher<>((env, prom) -> {
            setOAuthToken(acConfig, env.getContext());

            String argument = env.getArgument("filter");
            final Pattern pattern;
            if (argument != null && !argument.isEmpty()) {
                pattern = Pattern.compile(argument);
            } else {
                pattern = null;
            }

            createAdminClient(vertx, acConfig).onComplete(ac -> {
                if (ac.failed()) {
                    prom.fail(ac.cause());
                } else {
                    TopicOperations.getList(ac.result(), prom, pattern);
                }
            });
        });
        return dataFetcher;
    }
}
