/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package io.strimzi.admin.http.server.registration;

import io.strimzi.admin.http.server.AdminServer;
import io.vertx.core.Future;
import io.vertx.core.Vertx;

import java.util.Map;

/**
 * The RouteRegistration interface is used to identify modules that wish to expose a set of REST endpoints
 * on the {@link AdminServer}. The service returns a mount point and a
 * {@link io.vertx.ext.web.Router} containing the endpoints for the plugin.
 */
public interface RouteRegistration {
    Future<RouteRegistrationDescriptor> getRegistrationDescriptor(final Vertx vertx, Map<String, Object> config);
}

