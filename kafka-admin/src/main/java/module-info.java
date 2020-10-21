import io.strimzi.http.server.api.RouteRegistration;
import io.strimzi.kafka.admin.KafkaAdminService;

module io.strimzi.kafka.admin.api {
    requires io.strimzi.http.server.api;
    requires vertx.core;
    requires vertx.web.api.contract;
    requires vertx.web;
    requires org.slf4j;

    provides RouteRegistration with KafkaAdminService;
}