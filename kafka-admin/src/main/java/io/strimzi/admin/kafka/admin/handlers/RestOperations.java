/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package io.strimzi.admin.kafka.admin.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.strimzi.admin.kafka.admin.TopicOperations;
import io.strimzi.admin.kafka.admin.model.Types;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.ext.web.RoutingContext;
import org.apache.kafka.common.errors.InvalidRequestException;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class RestOperations extends CommonHandler implements OperationsHandler<Handler<RoutingContext>> {
    @Override
    public Handler<RoutingContext> createTopic(Map<String, Object> acConfig, Vertx vertx) {
        return routingContext -> {
            setOAuthToken(acConfig, routingContext);
            createAdminClient(vertx, acConfig).onComplete(ac -> {
                Types.NewTopic inputTopic = new Types.NewTopic();
                Promise<Types.NewTopic> prom = Promise.promise();
                ObjectMapper mapper = new ObjectMapper();
                try {
                    inputTopic = mapper.readValue(routingContext.getBody().getBytes(), Types.NewTopic.class);
                } catch (IOException e) {
                    routingContext.response().setStatusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.code());
                    routingContext.response().end(e.getMessage());
                    prom.fail(e);
                }

                if (ac.failed()) {
                    prom.fail(ac.cause());
                } else {
                    TopicOperations.createTopic(ac.result(), prom, inputTopic);
                }
                processResponse(prom, routingContext, HttpResponseStatus.CREATED);
            });
        };
    }

    @Override
    public Handler<RoutingContext> describeTopic(Map<String, Object> acConfig, Vertx vertx) {
        return routingContext -> {
            setOAuthToken(acConfig, routingContext);
            String uri = routingContext.request().uri();
            String topicToDescribe = uri.substring(uri.lastIndexOf("/") + 1);
            Promise<Types.Topic> prom = Promise.promise();
            if (topicToDescribe == null || topicToDescribe.isEmpty()) {
                prom.fail("Topic to describe has not been specified.");
                processResponse(prom, routingContext, HttpResponseStatus.BAD_REQUEST);
            }
            createAdminClient(vertx, acConfig).onComplete(ac -> {
                if (ac.failed()) {
                    prom.fail(ac.cause());
                } else {
                    TopicOperations.describeTopic(ac.result(), prom, topicToDescribe);
                }
                processResponse(prom, routingContext, HttpResponseStatus.OK);
            });
        };
    }

    @Override
    public Handler<RoutingContext> updateTopic(Map<String, Object> acConfig, Vertx vertx) {
        return routingContext -> {
            setOAuthToken(acConfig, routingContext);
            Promise<Types.UpdatedTopic> prom = Promise.promise();
            String uri = routingContext.request().uri();
            String topicToUpdate = uri.substring(uri.lastIndexOf("/") + 1);
            if (topicToUpdate == null || topicToUpdate.isEmpty()) {
                prom.fail("Topic to update has not been specified.");
                processResponse(prom, routingContext, HttpResponseStatus.BAD_REQUEST);
            }

            createAdminClient(vertx, acConfig).onComplete(ac -> {
                if (ac.failed()) {
                    prom.fail(ac.cause());
                } else {
                    Types.UpdatedTopic updatedTopic = new Types.UpdatedTopic();
                    ObjectMapper mapper = new ObjectMapper();
                    try {
                        updatedTopic = mapper.readValue(routingContext.getBody().getBytes(), Types.UpdatedTopic.class);
                        updatedTopic.setName(topicToUpdate);
                    } catch (IOException e) {
                        routingContext.response().setStatusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.code());
                        routingContext.response().end(e.getMessage());
                        prom.fail(e);
                    }
                    TopicOperations.updateTopic(ac.result(), updatedTopic, prom);
                }
                processResponse(prom, routingContext, HttpResponseStatus.OK);
            });
        };
    }

    @Override
    public Handler<RoutingContext> deleteTopic(Map<String, Object> acConfig, Vertx vertx) {
        return routingContext -> {
            setOAuthToken(acConfig, routingContext);
            String uri = routingContext.request().uri();
            String topicToDelete = uri.substring(uri.lastIndexOf("/") + 1);
            Promise<List<String>> prom = Promise.promise();
            if (topicToDelete == null || topicToDelete.isEmpty()) {
                prom.fail("Topic to delete has not been specified.");
                processResponse(prom, routingContext, HttpResponseStatus.BAD_REQUEST);
                return;
            }

            createAdminClient(vertx, acConfig).onComplete(ac -> {
                if (ac.failed()) {
                    prom.fail(ac.cause());
                } else {
                    TopicOperations.deleteTopics(ac.result(), Collections.singletonList(topicToDelete), prom);
                }
                processResponse(prom, routingContext, HttpResponseStatus.OK);
            });
        };
    }

    @Override
    public Handler<RoutingContext> listTopics(Map<String, Object> acConfig, Vertx vertx) {
        return routingContext -> {
            setOAuthToken(acConfig, routingContext);
            String filter = routingContext.queryParams().get("filter");
            String limit = routingContext.queryParams().get("limit") == null ? "0" : routingContext.queryParams().get("limit");
            String offset = routingContext.queryParams().get("offset") == null ? "0" : routingContext.queryParams().get("offset");
            final Pattern pattern;
            Promise<Types.TopicList> prom = Promise.promise();
            if (filter != null && !filter.isEmpty()) {
                pattern = Pattern.compile(filter);
            } else {
                pattern = null;
            }

            createAdminClient(vertx, acConfig).onComplete(ac -> {
                if (ac.failed()) {
                    prom.fail(ac.cause());
                } else {
                    try {
                        if (Integer.parseInt(offset) < 0 || Integer.parseInt(limit) < 0) {
                            throw new InvalidRequestException("Offset and limit have to be positive integers.");
                        }
                        TopicOperations.getList(ac.result(), prom, pattern, Integer.parseInt(offset), Integer.parseInt(limit));
                    } catch (NumberFormatException | InvalidRequestException e) {
                        prom.fail(e);
                        processResponse(prom, routingContext, HttpResponseStatus.BAD_REQUEST);
                    }
                }
                processResponse(prom, routingContext, HttpResponseStatus.OK);
            });
        };
    }
}
