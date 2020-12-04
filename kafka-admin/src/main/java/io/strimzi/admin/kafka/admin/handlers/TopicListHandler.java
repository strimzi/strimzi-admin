/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package io.strimzi.admin.kafka.admin.handlers;

import io.strimzi.admin.kafka.admin.AdminClientProvider;
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

public class TopicListHandler {

    public static VertxDataFetcher topicListFetch(AdminClientProvider acp) {
        VertxDataFetcher<Types.TopicList> dataFetcher = new VertxDataFetcher<>((env, prom) -> {
            RoutingContextWrapper routingContext = env.getContext();
            routingContext.request().headers().get("token");

            Promise<Set<String>> describeTopicsNamesPromise = Promise.promise();
            acp.listTopics(describeTopicsNamesPromise);
            describeTopicsNamesPromise.future()
                    .onComplete(topics -> {
                        Types.TopicList topicList = new Types.TopicList();
                        List<Types.Topic> items = new ArrayList<>();
                        topics.result().forEach(top -> {
                            if (byTopicName(env.getArgument("search")).test(top)) {
                                Types.Topic ton = new Types.Topic();
                                ton.setName(top);
                                items.add(ton);
                            }
                        });
                        topicList.setItems(items);
                        prom.complete(topicList);
                    });
        });
        return dataFetcher;
    }

    private static Predicate<String> byTopicName(String regexParam) {
        return topic -> {
            if (regexParam == null) {
                return true;
            } else {
                Pattern pattern = Pattern.compile(regexParam);
                Matcher matcher = pattern.matcher(topic);
                return matcher.find();
            }
        };
    }
}
