/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package io.strimzi.admin.kafka.admin;

import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;
import io.strimzi.admin.graphql.registration.GraphQLRegistration;
import io.strimzi.admin.graphql.registration.GraphQLRegistrationDescriptor;
import io.strimzi.admin.kafka.admin.handlers.TopicCreateHandler;
import io.strimzi.admin.kafka.admin.handlers.TopicsDeleteHandler;
import io.strimzi.admin.kafka.admin.handlers.TopicDescriptionHandler;
import io.strimzi.admin.kafka.admin.handlers.TopicListHandler;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;

/**
 * Defines the GraphQL schema and its implementation for the Kafka Admin client
 * queries and mutations.
 */
public class KafkaAdminService implements GraphQLRegistration {

    protected final Logger log = LogManager.getLogger(KafkaAdminService.class);
    private static final String KAFKA_ADMIN_SCHEMA_LOCATION = "graphql-schema/kafka-admin.graphql";
    private Map<String, Object> config;

    @Override
    public void setConfiguration(Map<String, Object> config) {
        this.config = config;
    }

    @Override
    public Future<GraphQLRegistrationDescriptor> getRegistrationDescriptor(final Vertx vertx) {
        final Promise<GraphQLRegistrationDescriptor> promise = Promise.promise();

        vertx.executeBlocking(p -> {
            try {
                final SchemaParser schemaParser = new SchemaParser();
                final InputStreamReader userInputStream = new InputStreamReader(
                    Objects.requireNonNull(
                        getClass()
                            .getClassLoader()
                            .getResourceAsStream(KAFKA_ADMIN_SCHEMA_LOCATION)),
                    StandardCharsets.UTF_8);

                final TypeDefinitionRegistry typeDefinitionRegistry = schemaParser.parse(userInputStream);
                p.complete(typeDefinitionRegistry);
            } catch (Exception exc) {
                p.fail(exc);
            }
        }, ar -> {
                final TypeDefinitionRegistry schema = (TypeDefinitionRegistry) ar.result();

                AdminClientWrapper acw = new AdminClientWrapper(vertx, config);
                try {
                    acw.open();
                } catch (Exception e) {
                    log.error("AdminClient with configuration {} cannot be created. Check whether the kafka cluster available.", config, e);
                    promise.fail(e);
                    return;
                }

                final RuntimeWiring query = RuntimeWiring.newRuntimeWiring()
                    .type("Query", typeWiring -> typeWiring
                        .dataFetcher("topic", TopicDescriptionHandler.topicDescriptionFetch(acw))
                        .dataFetcher("topicList", TopicListHandler.topicListFetch(acw))
                    )
                    .type("Mutation", typeWiring -> typeWiring
                            .dataFetcher("deleteTopics", TopicsDeleteHandler.deleteTopics(acw))
                            .dataFetcher("createTopic", TopicCreateHandler.createTopic(acw))
                    )
                    .build();

                promise.complete(GraphQLRegistrationDescriptor.create(schema, query));
            });

        return promise.future();
    }
}

