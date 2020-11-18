import io.strimzi.http.server.api.RouteRegistration;

module io.strimzi.http.server {
    requires vertx.core;
    requires vertx.web;
    requires org.slf4j;
    requires io.strimzi.http.server.api;

    uses RouteRegistration;
}