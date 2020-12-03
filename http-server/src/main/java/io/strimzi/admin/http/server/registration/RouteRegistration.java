package io.strimzi.admin.http.server.registration;

import io.strimzi.admin.http.server.AdminServer;
import io.vertx.core.Future;
import io.vertx.core.Vertx;

/**
 * The RouteRegistration interface is used to identify modules that wish to expose a set of REST endpoints
 * on the {@link AdminServer}. The service returns a mount point and a
 * {@link io.vertx.ext.web.Router} containing the endpoints for the plugin.
 */
public interface RouteRegistration {
    Future<RouteRegistrationDescriptor> getRegistrationDescriptor(final Vertx vertx);
}

