/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package io.strimzi.admin.kafka.admin.handlers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.strimzi.kafka.oauth.validator.TokenExpiredException;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.ext.web.RoutingContext;
import io.vertx.kafka.admin.KafkaAdminClient;
import org.apache.kafka.common.config.SaslConfigs;
import org.apache.kafka.common.errors.UnknownTopicOrPartitionException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;
import java.util.Properties;

public class CommonHandler {
    protected static final Logger log = LogManager.getLogger(CommonHandler.class);

    protected static void setOAuthToken(Map acConfig, RoutingContext rc) {
        String token = rc.request().getHeader("Authorization");
        if (token != null) {
            if (token.startsWith("Bearer ")) {
                token = token.substring("Bearer ".length());
            }
            acConfig.put(SaslConfigs.SASL_JAAS_CONFIG, "org.apache.kafka.common.security.oauthbearer.OAuthBearerLoginModule required oauth.access.token=\"" + token + "\";");
        }
    }

    protected static Future<KafkaAdminClient> createAdminClient(Vertx vertx, Map acConfig) {
        Properties props = new Properties();
        props.putAll(acConfig);

        KafkaAdminClient adminClient = null;
        try {
            adminClient = KafkaAdminClient.create(vertx, props);
            return Future.succeededFuture(adminClient);
        } catch (Exception e) {
            log.error("Failed to create Kafka AdminClient", e.getCause());
            if (adminClient != null) {
                adminClient.close();
            }
            return Future.failedFuture(e);
        }
    }

    protected static <T> void processResponse(Promise<T> prom, RoutingContext routingContext, HttpResponseStatus responseStatus) {
        prom.future().onComplete(res -> {
            if (res.failed()) {
                if (res.cause() instanceof UnknownTopicOrPartitionException) {
                    routingContext.response().setStatusCode(HttpResponseStatus.NOT_FOUND.code());
                } else if (res.cause() instanceof org.apache.kafka.common.errors.TimeoutException) {
                    routingContext.response().setStatusCode(HttpResponseStatus.REQUEST_TIMEOUT.code());
                } else if (res.cause() instanceof org.apache.kafka.common.errors.AuthenticationException ||
                    res.cause() instanceof org.apache.kafka.common.errors.AuthorizationException ||
                    res.cause() instanceof TokenExpiredException) {
                    routingContext.response().setStatusCode(HttpResponseStatus.UNAUTHORIZED.code());
                } else {
                    routingContext.response().setStatusCode(responseStatus.code());
                }
                routingContext.response().end(res.cause().getMessage());
            } else {
                ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
                String json = null;
                try {
                    json = ow.writeValueAsString(res.result());
                } catch (JsonProcessingException e) {
                    routingContext.response().setStatusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.code());
                    routingContext.response().end(e.getMessage());
                }
                routingContext.response().setStatusCode(responseStatus.code());
                routingContext.response().end(json);
            }
        });
    }
}
