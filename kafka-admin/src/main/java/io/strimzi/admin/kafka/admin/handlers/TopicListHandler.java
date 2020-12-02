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
import java.util.stream.Collectors;

public class TopicListHandler {

    public static VertxDataFetcher topicListFetch(AdminClientProvider acp) {
        VertxDataFetcher<List<Types.TopicOnlyName>> dataFetcher = new VertxDataFetcher<>((env, prom) -> {
            RoutingContextWrapper routingContext = env.getContext();
            routingContext.request().headers().get("token");

            Promise<Set<String>> describeTopicsNamesPromise = Promise.promise();
            acp.listTopics(describeTopicsNamesPromise);
            describeTopicsNamesPromise.future()
                    .onComplete(topics -> {
                        List<Types.TopicOnlyName> topicNamesList = new ArrayList<>();
                        topics.result().forEach(top -> {
                            Types.TopicOnlyName ton = new Types.TopicOnlyName();
                            ton.setName(top);
                            topicNamesList.add(ton);
                        });
                        prom.complete(topicNamesList.stream().filter(byTopicName(env.getArgument("name"))).collect(Collectors.toList()));
                    });
        });
        return dataFetcher;
    }

    private static Predicate<Types.TopicOnlyName> byTopicName(String filterParameter) {
        return topic -> {
            if (filterParameter == null) {
                return true;
            } else {
                return topic.getName().contains(filterParameter);
            }
        };
    }
}
