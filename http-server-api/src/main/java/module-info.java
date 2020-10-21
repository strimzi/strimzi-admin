import io.strimzi.http.server.api.RouteRegistration;

module io.strimzi.http.server.api {
    requires vertx.core;
    requires vertx.web;

    exports io.strimzi.http.server.api;
}