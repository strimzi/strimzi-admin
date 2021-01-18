/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package io.strimzi.admin.kafka.admin.handlers;

import io.strimzi.admin.common.data.fetchers.AdminClientWrapper;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.ext.web.RoutingContext;
import org.apache.kafka.common.config.SaslConfigs;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;

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

    protected static Future<AdminClientWrapper> createAdminClient(Vertx vertx, Map acConfig) {
        AdminClientWrapper acw = new AdminClientWrapper(vertx, acConfig);
        try {
            acw.open();
            return Future.succeededFuture(acw);
        } catch (Exception e) {
            log.error(e);
            if (acw != null) {
                acw.close();
            }
            return Future.failedFuture(e);
        }
    }
}
