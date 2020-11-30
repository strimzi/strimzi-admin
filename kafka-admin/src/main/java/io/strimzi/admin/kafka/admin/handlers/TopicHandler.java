package io.strimzi.admin.kafka.admin.handlers;

import graphql.schema.DataFetchingEnvironment;
import io.strimzi.admin.kafka.admin.model.Topic;
import io.vertx.core.Promise;

public class TopicHandler {

    public static void getTopic(final DataFetchingEnvironment env, final Promise<Topic> promise) {
        promise.complete(TopicListHandler.SAMPLE_TOPICS.get(env.getArgument("topicName")));
    }
}
