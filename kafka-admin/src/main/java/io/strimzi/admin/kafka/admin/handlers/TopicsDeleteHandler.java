/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package io.strimzi.admin.kafka.admin.handlers;

import io.strimzi.admin.Constants;
import io.strimzi.admin.kafka.admin.AdminClientWrapper;
import io.vertx.core.Vertx;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.graphql.VertxDataFetcher;
import java.util.List;
import java.util.Map;

public class TopicsDeleteHandler {

    public static VertxDataFetcher deleteTopics(Map<String, Object> acConfig, Vertx vertx) {
        VertxDataFetcher<List<String>> dataFetcher = new VertxDataFetcher<>((environment, prom) -> {
            RoutingContext rc = environment.getContext();
            if (rc.request().getHeader("Authorization") != null) {
                acConfig.put(Constants.SECURITY_SASL_JAAS_CONFIG, "org.apache.kafka.common.security.oauthbearer.OAuthBearerLoginModule required oauth.access.token=" + rc.request().getHeader("Authorization") + " ;");
            }

            AdminClientWrapper acw = new AdminClientWrapper(vertx, acConfig);
            try {
                acw.open();
            } catch (Exception e) {
                prom.fail(e);
                return;
            }

            List<String> topicsToDelete = environment.getArgument("names");
            acw.deleteTopics(topicsToDelete, res -> {
                if (res.failed()) {
                    prom.fail(res.cause());
                    acw.close();
                } else {
                    prom.complete(topicsToDelete);
                    acw.close();
                }
            });
        });
        return dataFetcher;
    }
}
