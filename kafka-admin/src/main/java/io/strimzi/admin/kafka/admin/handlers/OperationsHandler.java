/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package io.strimzi.admin.kafka.admin.handlers;

import io.strimzi.admin.kafka.admin.HttpMetrics;
import io.vertx.core.Vertx;

import java.util.Map;

public interface OperationsHandler<T extends Object> {
    public T createTopic(Map<String, Object> acConfig, Vertx vertx, HttpMetrics httpMetrics);
    public T describeTopic(Map<String, Object> acConfig, Vertx vertx, HttpMetrics httpMetrics);
    public T updateTopic(Map<String, Object> acConfig, Vertx vertx, HttpMetrics httpMetrics);
    public T deleteTopic(Map<String, Object> acConfig, Vertx vertx, HttpMetrics httpMetrics);
    public T listTopics(Map<String, Object> acConfig, Vertx vertx, HttpMetrics httpMetrics);
}
