/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package io.strimzi.admin.kafka.admin;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Timer;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import io.vertx.micrometer.backends.BackendRegistries;

public class HttpMetrics {
    private PrometheusMeterRegistry meterRegistry;
    private Counter requestsCounter;
    private Counter failedRequestsCounter;
    private Counter succeededRequestsCounter;
    private Counter deleteTopicCounter;
    private Counter createTopicCounter;
    private Counter updateTopicCounter;
    private Counter listTopicsCounter;
    private Counter describeTopicCounter;

    private Counter describeGroupCounter;
    private Counter listGroupsCounter;
    private Counter deleteGroupCounter;
    private Timer requestTimer;

    public HttpMetrics() {
        this.meterRegistry = (PrometheusMeterRegistry) BackendRegistries.getDefaultNow();
        init();
    }

    private void init() {
        requestsCounter = meterRegistry.counter("requests");
        failedRequestsCounter = meterRegistry.counter("failed_requests");
        succeededRequestsCounter = meterRegistry.counter("succeeded_requests");
        requestTimer = meterRegistry.timer("request_time");
        deleteTopicCounter = meterRegistry.counter("delete_topic_requests");
        createTopicCounter = meterRegistry.counter("create_topic_requests");
        updateTopicCounter = meterRegistry.counter("update_topic_requests");
        listTopicsCounter = meterRegistry.counter("list_topics_requests");
        describeTopicCounter = meterRegistry.counter("describe_topic_requests");

        listGroupsCounter = meterRegistry.counter("list_groups_requests");
        describeGroupCounter = meterRegistry.counter("get_group_requests");
        deleteGroupCounter = meterRegistry.counter("delete_group_requests");
    }

    public PrometheusMeterRegistry getRegistry() {
        return meterRegistry;
    }

    public Counter getFailedRequestsCounter() {
        return failedRequestsCounter;
    }

    public Counter getRequestsCounter() {
        return requestsCounter;
    }

    public Counter getSucceededRequestsCounter() {
        return succeededRequestsCounter;
    }

    public Timer getRequestTimer() {
        return requestTimer;
    }

    public Counter getCreateTopicCounter() {
        return createTopicCounter;
    }

    public Counter getDeleteTopicCounter() {
        return deleteTopicCounter;
    }

    public Counter getDescribeTopicCounter() {
        return describeTopicCounter;
    }

    public Counter getListTopicsCounter() {
        return listTopicsCounter;
    }

    public Counter getUpdateTopicCounter() {
        return updateTopicCounter;
    }

    public Counter getDeleteGroupCounter() {
        return deleteGroupCounter;
    }

    public Counter getDescribeGroupCounter() {
        return describeGroupCounter;
    }

    public Counter getListGroupsCounter() {
        return listGroupsCounter;
    }
}
