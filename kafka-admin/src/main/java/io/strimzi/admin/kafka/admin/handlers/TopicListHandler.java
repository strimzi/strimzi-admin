/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package io.strimzi.admin.kafka.admin.handlers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.strimzi.admin.common.data.fetchers.AdminClientWrapper;
import io.strimzi.admin.common.data.fetchers.TopicOperations;
import io.strimzi.admin.common.data.fetchers.model.Types;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.graphql.VertxDataFetcher;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;
import java.util.regex.Pattern;

public class TopicListHandler extends CommonHandler {
    protected static final Logger log = LogManager.getLogger(TopicListHandler.class);

    public static VertxDataFetcher topicListFetcher(Map<String, Object> acConfig, Vertx vertx) {
        VertxDataFetcher<Types.TopicList> dataFetcher = new VertxDataFetcher<>((env, prom) -> {
            setOAuthToken(acConfig, env.getContext());
            Future<AdminClientWrapper> acw = createAdminClient(vertx, acConfig);

            String argument = env.getArgument("search");
            final Pattern pattern;
            if (argument != null && !argument.isEmpty()) {
                pattern = Pattern.compile(argument);
            } else {
                pattern = null;
            }

            TopicOperations.getList(acw, prom, pattern);
        });
        return dataFetcher;
    }

    public static Handler<RoutingContext> topicListHandle(Map<String, Object> acConfig, Vertx vertx) {
        return routingContext -> {
            setOAuthToken(acConfig, routingContext);
            Future<AdminClientWrapper> acw = createAdminClient(vertx, acConfig);

            String argument = routingContext.queryParams().get("search");
            final Pattern pattern;
            Promise<Types.TopicList> prom = Promise.promise();
            if (argument != null && !argument.isEmpty()) {
                pattern = Pattern.compile(argument);
            } else {
                pattern = null;
            }

            TopicOperations.getList(acw, prom, pattern);
            prom.future().onComplete(res -> {
                if (res.failed()) {
                    routingContext.response().setStatusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.code());
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
                    routingContext.response().setStatusCode(HttpResponseStatus.OK.code());
                    routingContext.response().end(json);
                }
            });
        };
    }
}
