/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package io.strimzi.admin.kafka.admin.handlers;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.strimzi.admin.kafka.admin.TopicOperations;
import io.strimzi.admin.kafka.admin.model.Types;
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

            String argument = env.getArgument("filter");
            final Pattern pattern;
            if (argument != null && !argument.isEmpty()) {
                pattern = Pattern.compile(argument);
            } else {
                pattern = null;
            }

            createAdminClient(vertx, acConfig).onComplete(ac -> {
                if (ac.failed()) {
                    prom.fail(ac.cause());
                } else {
                    TopicOperations.getList(ac.result(), prom, pattern);
                }
            });
        });
        return dataFetcher;
    }

    public static Handler<RoutingContext> topicListHandle(Map<String, Object> acConfig, Vertx vertx) {
        return routingContext -> {
            setOAuthToken(acConfig, routingContext);
            String argument = routingContext.queryParams().get("filter");
            final Pattern pattern;
            Promise<Types.TopicList> prom = Promise.promise();
            if (argument != null && !argument.isEmpty()) {
                pattern = Pattern.compile(argument);
            } else {
                pattern = null;
            }

            createAdminClient(vertx, acConfig).onComplete(ac -> {
                if (ac.failed()) {
                    prom.fail(ac.cause());
                } else {
                    TopicOperations.getList(ac.result(), prom, pattern);
                }
                processResponse(prom, routingContext, HttpResponseStatus.OK);
            });
        };
    }
}
