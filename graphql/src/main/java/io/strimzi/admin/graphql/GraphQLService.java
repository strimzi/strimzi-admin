package io.strimzi.admin.graphql;

import graphql.GraphQL;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;
import io.strimzi.admin.graphql.registration.GraphQLRegistration;
import io.strimzi.admin.graphql.registration.GraphQLRegistrationDescriptor;
import io.strimzi.admin.graphql.registration.RuntimeWiringRegistry;
import io.strimzi.admin.http.server.registration.RouteRegistration;
import io.strimzi.admin.http.server.registration.RouteRegistrationDescriptor;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.graphql.GraphQLHandler;
import io.vertx.ext.web.handler.graphql.GraphiQLHandler;
import io.vertx.ext.web.handler.graphql.GraphiQLHandlerOptions;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.ServiceLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class GraphQLService implements RouteRegistration {

    private static final Logger LOGGER = LogManager.getLogger(GraphQLService.class);
    private static final String BASE_SCHEMA_LOCATION = "graphql-schema/baseSchema.graphql";

    @Override
    public Future<RouteRegistrationDescriptor> getRegistrationDescriptor(Vertx vertx) {
        final Promise<RouteRegistrationDescriptor> promise = Promise.promise();

        final Router router = Router.router(vertx);
        final RouteRegistrationDescriptor graphQLRegistrationDescriptor = RouteRegistrationDescriptor.create("/", router);

        if (GraphiQLHandlerOptions.DEFAULT_ENABLED) {
            LOGGER.warn("GraphiQL is enabled");
            router.route("/graphiql/*").handler(GraphiQLHandler.create());
        }

        configureGraphQLHandler(vertx)
            .onSuccess(graphQLHandler -> {
                graphQLHandler.queryContext(routingContext -> routingContext);
                router.post("/graphql").handler(graphQLHandler);
                promise.complete(graphQLRegistrationDescriptor);
            })
            .onFailure(throwable -> {
                LOGGER.error("GraphQL service failed to initialize - {} ", throwable.getMessage());
                promise.fail(throwable);
            });

        return promise.future();
    }

    private Future<GraphQLHandler> configureGraphQLHandler(final Vertx vertx) {
        Promise<GraphQLHandler> promise = Promise.promise();

        vertx.executeBlocking(p -> {
                final SchemaParser schemaParser = new SchemaParser();
                final InputStreamReader userInputStream = new InputStreamReader(
                    Objects.requireNonNull(
                        getClass()
                            .getClassLoader()
                            .getResourceAsStream(GraphQLService.BASE_SCHEMA_LOCATION)),
                    StandardCharsets.UTF_8);

                final TypeDefinitionRegistry typeDefinitionRegistry = schemaParser.parse(userInputStream);
                p.complete(typeDefinitionRegistry);

            }, ar -> {
                if (ar.succeeded()) {
                    setupGraphQL(vertx, (TypeDefinitionRegistry) ar.result())
                        .onSuccess(graphQL -> promise.complete(GraphQLHandler.create(graphQL)))
                        .onFailure(promise::fail);
                } else {
                    promise.fail(ar.cause());
                }
            }
        );

        return promise.future();
    }

    private Future<GraphQL> setupGraphQL(final Vertx vertx, final TypeDefinitionRegistry baseSchemaRegistry) {
        final Promise<GraphQL> promise = Promise.promise();

        final ServiceLoader<GraphQLRegistration> loader = ServiceLoader.load(GraphQLRegistration.class);
        final List<Future<GraphQLRegistrationDescriptor>> registrationDescriptors = new ArrayList<>();

        loader.forEach(graphQLRegistration -> registrationDescriptors.add(graphQLRegistration.getRegistrationDescriptor(vertx)));

        CompositeFuture.all(new ArrayList<>(registrationDescriptors))
            .onSuccess(cf -> {
                final RuntimeWiringRegistry runtimeWiringRegistry = new RuntimeWiringRegistry();

                registrationDescriptors.forEach(future -> {
                    baseSchemaRegistry.merge(future.result().getTypeDefinitionRegistry());
                    runtimeWiringRegistry.add(future.result().getRuntimeWiring());
                });

                final SchemaGenerator schemaGenerator = new SchemaGenerator();
                final GraphQLSchema schema = GraphQLSchema.newSchema(
                    schemaGenerator.makeExecutableSchema(
                        baseSchemaRegistry,
                        runtimeWiringRegistry.getRuntimeWiring()))
                    .build();

                promise.complete(GraphQL.newGraphQL(schema).build());
            })
            .onFailure(promise::fail);

        return promise.future();
    }
}
