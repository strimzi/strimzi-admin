package io.strimzi.http.server;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;
import org.javatuples.Triplet;

/**
 * The RestService interface is used to identify modules that wish to expose a set of REST endpoints
 * on the {@link AdminServer}. The service returns a plugin name, a mount
 * point and a {@link io.vertx.ext.web.Router} containing the endpoints for the plugin.
 */
public interface RestService {
    Future<Triplet<String, String, Router>> registerRoutes(Vertx vertx);
}
