/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package io.strimzi.admin.kafka.admin.handlers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import io.micrometer.core.instrument.Timer;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.strimzi.admin.kafka.admin.HttpMetrics;
import io.strimzi.admin.kafka.admin.model.Types;
import io.strimzi.kafka.oauth.validator.TokenExpiredException;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.ext.web.RoutingContext;
import io.vertx.kafka.admin.KafkaAdminClient;
import org.apache.kafka.common.config.SaslConfigs;
import org.apache.kafka.common.errors.AuthenticationException;
import org.apache.kafka.common.errors.AuthorizationException;
import org.apache.kafka.common.errors.InvalidReplicationFactorException;
import org.apache.kafka.common.errors.InvalidRequestException;
import org.apache.kafka.common.errors.InvalidTopicException;
import org.apache.kafka.common.errors.TimeoutException;
import org.apache.kafka.common.errors.TopicExistsException;
import org.apache.kafka.common.errors.UnknownTopicOrPartitionException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Comparator;
import java.util.Map;
import java.util.Properties;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

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

    protected static <T> void processResponse(Promise<T> prom, RoutingContext routingContext, HttpResponseStatus responseStatus, HttpMetrics httpMetrics, Timer.Sample requestTimerSample) {
        prom.future().onComplete(res -> {
            if (res.failed()) {
                if (res.cause() instanceof UnknownTopicOrPartitionException) {
                    routingContext.response().setStatusCode(HttpResponseStatus.NOT_FOUND.code());
                } else if (res.cause() instanceof TimeoutException) {
                    routingContext.response().setStatusCode(HttpResponseStatus.SERVICE_UNAVAILABLE.code());
                } else if (res.cause() instanceof AuthenticationException ||
                    res.cause() instanceof AuthorizationException ||
                    res.cause() instanceof TokenExpiredException) {
                    routingContext.response().setStatusCode(HttpResponseStatus.UNAUTHORIZED.code());
                } else if (res.cause() instanceof InvalidTopicException) {
                    routingContext.response().setStatusCode(HttpResponseStatus.BAD_REQUEST.code());
                } else if (res.cause() instanceof InvalidReplicationFactorException) {
                    routingContext.response().setStatusCode(HttpResponseStatus.BAD_REQUEST.code());
                } else if (res.cause() instanceof TopicExistsException) {
                    routingContext.response().setStatusCode(HttpResponseStatus.CONFLICT.code());
                } else if (res.cause() instanceof InvalidRequestException) {
                    routingContext.response().setStatusCode(HttpResponseStatus.BAD_REQUEST.code());
                } else {
                    routingContext.response().setStatusCode(responseStatus.code());
                }
                routingContext.response().end(res.cause().getMessage());
                httpMetrics.getFailedRequestsCounter().increment();
                requestTimerSample.stop(httpMetrics.getRequestTimer());
            } else {
                ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
                String json = null;
                try {
                    json = ow.writeValueAsString(res.result());
                } catch (JsonProcessingException e) {
                    routingContext.response().setStatusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.code());
                    routingContext.response().end(e.getMessage());
                    httpMetrics.getFailedRequestsCounter().increment();
                    requestTimerSample.stop(httpMetrics.getRequestTimer());
                }
                routingContext.response().setStatusCode(responseStatus.code());
                routingContext.response().end(json);
                httpMetrics.getSucceededRequestsCounter().increment();
                requestTimerSample.stop(httpMetrics.getRequestTimer());
            }
        });
    }

    public static class TopicComparator implements Comparator<Types.Topic> {
        @Override
        public int compare(Types.Topic firstTopic, Types.Topic secondTopic) {
            return firstTopic.getName().compareTo(secondTopic.getName());
        }
    }

    public static class ConsumerGroupComparator implements Comparator<Types.ConsumerGroup> {
        @Override
        public int compare(Types.ConsumerGroup firstConsumerGroup, Types.ConsumerGroup secondConsumerGroup) {
            return firstConsumerGroup.getId().compareTo(secondConsumerGroup.getId());
        }
    }

    public static Predicate<String> byName(Pattern pattern, Promise prom) {
        return topic -> {
            if (pattern == null) {
                return true;
            } else {
                try {
                    Matcher matcher = pattern.matcher(topic);
                    return matcher.find();
                } catch (PatternSyntaxException ex) {
                    prom.fail(ex);
                    return false;
                }
            }
        };
    }
}
