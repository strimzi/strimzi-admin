/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package io.strimzi.admin.kafka.admin.handlers;

import io.strimzi.admin.kafka.admin.AdminClientWrapper;
import io.strimzi.admin.kafka.admin.model.Types;
import io.vertx.core.Promise;
import io.vertx.ext.web.handler.graphql.VertxDataFetcher;
import io.vertx.ext.web.impl.RoutingContextWrapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class TopicListHandler {

    public static VertxDataFetcher topicListFetch(AdminClientWrapper acw) {
        VertxDataFetcher<Types.TopicList> dataFetcher = new VertxDataFetcher<>((env, prom) -> {
            RoutingContextWrapper routingContext = env.getContext();
            routingContext.request().headers().get("token");

            Promise<Set<String>> describeTopicsNamesPromise = Promise.promise();
            acw.listTopics(describeTopicsNamesPromise);
            describeTopicsNamesPromise.future()
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
