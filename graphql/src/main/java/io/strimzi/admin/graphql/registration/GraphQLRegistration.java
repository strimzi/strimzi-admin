package io.strimzi.admin.graphql.registration;

import io.vertx.core.Future;
import io.vertx.core.Vertx;

import java.util.Map;

/**
 * An interface representing the registration of a GraphQL schema and its implementation
 */
public interface GraphQLRegistration {
    /**
     * Used to retrieve a {@link GraphQLRegistrationDescriptor} defining the schema and runtime
     * for a GraphQL module.
     * @param vertx a running instance of a {@link io.vertx.core.Vertx}
     * @return a future descriptor containing the schema and the implementation
     */
    Future<GraphQLRegistrationDescriptor> getRegistrationDescriptor(final Vertx vertx);

    void setConfiguration(Map<String, Object> config);
}
