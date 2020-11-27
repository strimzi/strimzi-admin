package io.strimzi.admin.kafka.admin.handlers;

import graphql.schema.DataFetchingEnvironment;
import io.strimzi.admin.kafka.admin.model.Topic;
import io.vertx.core.Promise;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class TopicListHandler {
    private static final List<Topic> sampleTopics = new ArrayList<>();
    static {
        sampleTopics.add(Topic.create("topic1", false, 3, 3));
        sampleTopics.add(Topic.create("topic2-a", false, 1, 1));
        sampleTopics.add(Topic.create("topic2-b", false, 1, 1));
        sampleTopics.add(Topic.create("topic2-c", false, 1, 1));
        sampleTopics.add(Topic.create("topic3", false, 6, 1));
    }

    public static void getTopicList(final DataFetchingEnvironment env, final Promise<List<Topic>> promise) {
        promise.complete(
            sampleTopics
                .stream()
                .filter(byTopicName(env.getArgument("filter")))
                .collect(Collectors.toList()));
    }

    private static Predicate<Topic> byTopicName(String filterParameter) {
        return topic -> {
            if (filterParameter == null) {
                return true;
            } else {
                return topic.getName().contains(filterParameter);
            }
        };
    }
}
