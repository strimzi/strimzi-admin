package io.strimzi.admin.graphql.registration;

import io.vertx.core.Future;
import io.vertx.core.Vertx;

public interface GraphQLRegistration {
    Future<GraphQLRegistrationDescriptor> getRegistrationDescriptor(final Vertx vertx);
}
