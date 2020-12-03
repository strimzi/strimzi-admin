package io.strimzi.admin.kafka.admin.handlers;

import graphql.schema.DataFetchingEnvironment;
import io.strimzi.admin.kafka.admin.model.Topic;
import io.vertx.core.Promise;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class TopicListHandler {

    public static final Map<String, Topic> SAMPLE_TOPICS = new HashMap<>();
    static {
        SAMPLE_TOPICS.put("topic1", Topic.create("topic1", false, 3, 3));
        SAMPLE_TOPICS.put("topic2", Topic.create("topic2", false, 1, 1));
        SAMPLE_TOPICS.put("topic2-a", Topic.create("topic2-a", false, 1, 1));
        SAMPLE_TOPICS.put("topic2-b", Topic.create("topic2-b", false, 1, 1));
        SAMPLE_TOPICS.put("topic2-c", Topic.create("topic2-c", false, 1, 1));
        SAMPLE_TOPICS.put("topic3", Topic.create("topic3", false, 6, 1));
    }

    public static void getTopicList(final DataFetchingEnvironment env, final Promise<List<Topic>> promise) {
        promise.complete(
            SAMPLE_TOPICS
                .values()
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
