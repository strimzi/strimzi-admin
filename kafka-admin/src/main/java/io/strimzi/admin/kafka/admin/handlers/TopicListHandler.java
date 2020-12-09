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
import org.apache.kafka.common.config.SaslConfigs;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class TopicListHandler {

    public static VertxDataFetcher topicListFetch(Map<String, Object> acConfig, Vertx vertx) {
        VertxDataFetcher<Types.TopicList> dataFetcher = new VertxDataFetcher<>((env, prom) -> {
            RoutingContext rc = env.getContext();
            if (rc.request().getHeader("Authorization") != null) {
                acConfig.put(SaslConfigs.SASL_JAAS_CONFIG, "org.apache.kafka.common.security.oauthbearer.OAuthBearerLoginModule required oauth.access.token=" + rc.request().getHeader("Authorization") + " ;");
            }

            AdminClientWrapper acw = new AdminClientWrapper(vertx, acConfig);
            try {
                acw.open();
            } catch (Exception e) {
                prom.fail(e);
                return;
            }

            Promise<Set<String>> describeTopicsNamesPromise = Promise.promise();
            acw.listTopics(describeTopicsNamesPromise);
            describeTopicsNamesPromise.future().onFailure(
                fail -> {
                    prom.fail(fail);
                    return;
                })
                    .onComplete(topics -> {
                        Types.TopicList topicList = new Types.TopicList();
                        List<Types.Topic> items = new ArrayList<>();

                        String argument = env.getArgument("search");
                        final Pattern pattern;
                        if (argument != null && !argument.isEmpty()) {
                            pattern = Pattern.compile(argument);
                        } else {
                            pattern = null;
                        }
                        topics.result().forEach(topic -> {
                            if (byTopicName(pattern, prom).test(topic)) {
                                Types.Topic topicListEntry = new Types.Topic();
                                topicListEntry.setName(topic);
                                items.add(topicListEntry);
                            }
                        });
                        topicList.setItems(items);
                        prom.complete(topicList);
                        acw.close();
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
