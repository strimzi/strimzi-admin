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
import io.strimzi.admin.kafka.admin.handlers.TopicUpdateHandler;
import io.strimzi.admin.kafka.admin.handlers.TopicsDeleteHandler;
import io.strimzi.admin.kafka.admin.handlers.TopicDescriptionHandler;
import io.strimzi.admin.kafka.admin.handlers.TopicListHandler;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.config.SaslConfigs;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Defines the GraphQL schema and its implementation for the Kafka Admin client
 * queries and mutations.
 */
public class KafkaAdminService implements GraphQLRegistration {

    protected final Logger log = LogManager.getLogger(KafkaAdminService.class);
    private static final String KAFKA_ADMIN_SCHEMA_LOCATION = "graphql-schema/kafka-admin.graphql";
    private static final String PREFIX = "KAFKA_ADMIN_";
    private static Map<String, Object> config;

    public KafkaAdminService() throws Exception {
        config = envVarsToAdminClientConfig(PREFIX);
        logConfiguration();
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

                final RuntimeWiring query = RuntimeWiring.newRuntimeWiring()
                    .type("Query", typeWiring -> typeWiring
                        .dataFetcher("topic", TopicDescriptionHandler.topicDescriptionFetcher(config, vertx))
                        .dataFetcher("topicList", TopicListHandler.topicListFetcher(config, vertx))
                    )
                    .type("Mutation", typeWiring -> typeWiring
                            .dataFetcher("deleteTopics", TopicsDeleteHandler.deleteTopicsFetcher(config, vertx))
                            .dataFetcher("createTopic", TopicCreateHandler.createTopicFetcher(config, vertx))
                            .dataFetcher("updateTopic", TopicUpdateHandler.updateTopicFetcher(config, vertx))
                    )
                    .build();
                promise.complete(GraphQLRegistrationDescriptor.create(schema, query));
                log.info("Kafka admin service started.");
            });

        return promise.future();
    }

    private Map envVarsToAdminClientConfig(String prefix) throws Exception {
        Map envConfig = System.getenv().entrySet().stream().filter(entry -> entry.getKey().startsWith(prefix)).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        Map<String, String> adminClientConfig = new HashMap();
        if (envConfig.get(PREFIX + "BOOTSTRAP_SERVERS") == null) {
            throw new Exception("Bootstrap address has to be specified");
        }
        adminClientConfig.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, envConfig.get(PREFIX + "BOOTSTRAP_SERVERS").toString());

        // oAuth
        if (System.getenv(PREFIX + "OAUTH_ENABLED") == null ? true : Boolean.valueOf(System.getenv(PREFIX + "OAUTH_ENABLED"))) {
            log.info("oAuth enabled");
            adminClientConfig.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SASL_PLAINTEXT");
            adminClientConfig.put(SaslConfigs.SASL_MECHANISM, "OAUTHBEARER");
            adminClientConfig.put(SaslConfigs.SASL_LOGIN_CALLBACK_HANDLER_CLASS, "io.strimzi.kafka.oauth.client.JaasClientOauthLoginCallbackHandler");
        } else {
            log.info("oAuth disabled");
        }
        // admin client
        adminClientConfig.put(AdminClientConfig.METADATA_MAX_AGE_CONFIG, "30000");
        adminClientConfig.put(AdminClientConfig.REQUEST_TIMEOUT_MS_CONFIG, "10000");
        adminClientConfig.put(AdminClientConfig.DEFAULT_API_TIMEOUT_MS_CONFIG, "30000");

        return adminClientConfig;
    }

    private void logConfiguration() {
        log.info("AdminClient configuration:");
        config.entrySet().forEach(entry -> {
            log.info("\t{} = {}", entry.getKey(), entry.getValue());
        });
    }

    public static Map getAcConfig() {
        return config;
    }
}

