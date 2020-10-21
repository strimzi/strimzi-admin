package io.strimzi.http.server.api;

import io.vertx.core.Future;
import io.vertx.core.Vertx;

/**
 * The RouteRegistration interface is used to identify modules that wish to expose a set of REST endpoints
 * on the Admin Server. The service returns a future that resolves to a
 * {@link io.strimzi.http.server.api.RouteRegistrationDescriptor} when the future completes.
 */
public interface RouteRegistration {
    Future<RouteRegistrationDescriptor> registerRoutes(Vertx vertx);
}
