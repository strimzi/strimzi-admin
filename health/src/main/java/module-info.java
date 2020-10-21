import io.strimzi.admin.health.Health;
import io.strimzi.http.server.api.RouteRegistration;

module io.strimzi.admin.health.api {
    requires io.strimzi.http.server.api;
    requires vertx.web.api.contract;
    requires vertx.core;
    requires vertx.web;
    requires org.slf4j;

    provides RouteRegistration with Health;
}