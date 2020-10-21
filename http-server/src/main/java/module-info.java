import io.strimzi.http.server.api.RouteRegistration;

module io.strimzi.http.server {
    requires io.strimzi.http.server.api;
    requires vertx.core;
    requires vertx.web;
    requires org.slf4j;

    uses RouteRegistration;
}