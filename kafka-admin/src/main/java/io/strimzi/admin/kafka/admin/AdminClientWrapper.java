/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package io.strimzi.admin.kafka.admin;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.kafka.admin.Config;
import io.vertx.kafka.admin.KafkaAdminClient;
import io.vertx.kafka.admin.NewTopic;
import io.vertx.kafka.admin.TopicDescription;
import io.vertx.kafka.client.common.ConfigResource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * class for admin client wrapper
 */
public class AdminClientWrapper {
    protected final Logger log = LogManager.getLogger(AdminClientWrapper.class);

    protected final Vertx vertx;
    protected final Map<String, Object> config;

    private Handler<AdminClientWrapper> closeHandler;

    private KafkaAdminClient adminClient;

    /**
     * Constructor
     *
     * @param vertx Vert.x instance
     * @param config configuration
     */
    public AdminClientWrapper(Vertx vertx, Map<String, Object> config) {
        this.vertx = vertx;
        this.config = config;
    }

    public AdminClientWrapper closeHandler(Handler<AdminClientWrapper> endpointCloseHandler) {
        this.closeHandler = endpointCloseHandler;
        return this;
    }

    public void open() {
        // create an admin client
        Map<String, Object> kafkaConfig = this.config;
        Properties props = new Properties();
        props.putAll(kafkaConfig);

        try {
            this.adminClient = KafkaAdminClient.create(this.vertx, props);
        } catch (Exception e) {
            throw e;
        }
    }

    public void close() {
        if (this.adminClient != null) {
            this.adminClient.close();
        }
        this.handleClose();
    }

    /**
     * Returns all the topics.
     */
    public void listTopics(Handler<AsyncResult<Set<String>>> handler) {
        log.info("List topics");
        this.adminClient.listTopics(handler);
    }

    /**
     * Creates a topic.
     */
    public void createTopic(List<NewTopic> topics, Handler<AsyncResult<Void>> completionHandler) {
        log.info("Create topic");
        this.adminClient.createTopics(topics, completionHandler);
    }

    /**
     * Deletes a topic.
     */
    public void deleteTopics(List<String> topics, Handler<AsyncResult<Void>> completionHandler) {
        log.info("Delete topics");
        this.adminClient.deleteTopics(topics, completionHandler);
    }

    /**
     * Returns the description of the specified topics.
     */
    public void describeTopics(List<String> topicNames, Handler<AsyncResult<Map<String, TopicDescription>>> handler) {
        log.info("Describe topics {}", topicNames);
        this.adminClient.describeTopics(topicNames, handler);
    }

    /**
     * Returns the configuration of the specified resources.
     */
    public void describeConfigs(List<ConfigResource> configResources, Handler<AsyncResult<Map<ConfigResource, Config>>> handler) {
        log.info("Describe configs {}", configResources);
        this.adminClient.describeConfigs(configResources, handler);
    }

    /**
     * Raise close event
     */
    protected void handleClose() {

        if (this.closeHandler != null) {
            this.closeHandler.handle(this);
        }
    }
}
