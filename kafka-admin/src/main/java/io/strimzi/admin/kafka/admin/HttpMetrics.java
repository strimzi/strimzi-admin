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
    private Counter deleteCounter;
    private Counter createCounter;
    private Counter updateCounter;
    private Counter listCounter;
    private Counter describeCounter;
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
        deleteCounter = meterRegistry.counter("delete_requests");
        createCounter = meterRegistry.counter("create_requests");
        updateCounter = meterRegistry.counter("update_requests");
        listCounter = meterRegistry.counter("list_requests");
        describeCounter = meterRegistry.counter("describe_requests");
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

    public Counter getCreateCounter() {
        return createCounter;
    }

    public Counter getDeleteCounter() {
        return deleteCounter;
    }

    public Counter getDescribeCounter() {
        return describeCounter;
    }

    public Counter getListCounter() {
        return listCounter;
    }

    public Counter getUpdateCounter() {
        return updateCounter;
    }
}
