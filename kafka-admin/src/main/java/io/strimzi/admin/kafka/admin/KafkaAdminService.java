package io.strimzi.admin.kafka.admin;

import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;
import io.strimzi.admin.graphql.registration.GraphQLRegistration;
import io.strimzi.admin.graphql.registration.GraphQLRegistrationDescriptor;
import io.strimzi.admin.kafka.admin.handlers.TopicCreateHandler;
import io.strimzi.admin.kafka.admin.handlers.TopicDeleteHandler;
import io.strimzi.admin.kafka.admin.handlers.TopicDescriptionHandler;
import io.strimzi.admin.kafka.admin.handlers.TopicListHandler;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;

/**
 * Defines the GraphQL schema and its implementation for the Kafka Admin client
 * queries and mutations.
 */
public class KafkaAdminService implements GraphQLRegistration {
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
            }
            catch (Exception exc) {
                p.fail(exc);
            }
        }, ar -> {
            final TypeDefinitionRegistry schema = (TypeDefinitionRegistry) ar.result();

            AdminClientProvider acp = new AdminClientProvider(vertx, config);
            acp.open();
            // todo close acp
            final RuntimeWiring query = RuntimeWiring.newRuntimeWiring()
                .type("Query", typeWiring -> typeWiring
                    .dataFetcher("topicDescription", TopicDescriptionHandler.topicDescriptionFetch(acp))
                    .dataFetcher("topicList", TopicListHandler.topicListFetch(acp))
                )
                .type("Mutation", typeWiring -> typeWiring
                        .dataFetcher("deleteTopic", TopicDeleteHandler.deleteTopic(acp))
                        .dataFetcher("createTopic", TopicCreateHandler.createTopic(acp))
                )
                .build();

            promise.complete(GraphQLRegistrationDescriptor.create(schema, query));
        });

        return promise.future();
    }
}

