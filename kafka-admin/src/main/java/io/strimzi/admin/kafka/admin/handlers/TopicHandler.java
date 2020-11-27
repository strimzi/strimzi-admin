package io.strimzi.admin.kafka.admin.handlers;

import graphql.schema.DataFetchingEnvironment;
import io.strimzi.admin.kafka.admin.model.Topic;
import io.vertx.core.Promise;
import java.util.HashMap;
import java.util.Map;

public class TopicHandler {
    private static final Map<String, Topic> sampleTopics = new HashMap<>();
    static {
        sampleTopics.put("topic1", Topic.create("topic1", false, 3, 3));
        sampleTopics.put("topic2", Topic.create("topic2", false, 1, 1));
        sampleTopics.put("topic2-a", Topic.create("topic2-a", false, 1, 1));
        sampleTopics.put("topic2-b", Topic.create("topic2-b", false, 1, 1));
        sampleTopics.put("topic2-c", Topic.create("topic2-c", false, 1, 1));
        sampleTopics.put("topic3", Topic.create("topic3", false, 6, 1));
    }

    public static void getTopic(final DataFetchingEnvironment env, final Promise<Topic> promise) {
        promise.complete(sampleTopics.get(env.getArgument("topicName")));
    }
}
