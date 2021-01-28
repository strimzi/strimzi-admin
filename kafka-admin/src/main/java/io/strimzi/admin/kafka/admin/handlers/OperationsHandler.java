/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package io.strimzi.admin.kafka.admin.handlers;

import io.vertx.core.Vertx;

import java.util.Map;

public interface OperationsHandler<T extends Object> {
    public T createTopic(Map<String, Object> acConfig, Vertx vertx);
    public T describeTopic(Map<String, Object> acConfig, Vertx vertx);
    public T updateTopic(Map<String, Object> acConfig, Vertx vertx);
    public T deleteTopic(Map<String, Object> acConfig, Vertx vertx);
    public T listTopics(Map<String, Object> acConfig, Vertx vertx);
}
