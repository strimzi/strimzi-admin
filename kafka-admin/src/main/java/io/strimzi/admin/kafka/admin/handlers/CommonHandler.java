/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package io.strimzi.admin.kafka.admin.handlers;

import graphql.schema.DataFetchingEnvironment;
import io.strimzi.admin.kafka.admin.AdminClientWrapper;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.ext.web.RoutingContext;
import org.apache.kafka.common.config.SaslConfigs;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;

public class CommonHandler {
    protected static final Logger log = LogManager.getLogger(CommonHandler.class);

    protected static void adjustToken(Map acConfig, DataFetchingEnvironment env) {
        RoutingContext rc = env.getContext();
        String token = rc.request().getHeader("Authorization");
        if (token != null) {
            if (token.startsWith("Bearer ")) {
                token = token.substring("Bearer ".length());
            }
            log.info("auth token is {}", token);
            log.info(SaslConfigs.SASL_JAAS_CONFIG + "is org.apache.kafka.common.security.oauthbearer.OAuthBearerLoginModule required oauth.access.token=\"" + token + "\";");
            acConfig.put(SaslConfigs.SASL_JAAS_CONFIG, "org.apache.kafka.common.security.oauthbearer.OAuthBearerLoginModule required oauth.access.token=\"" + token + "\";");
        }
    }

    protected static AdminClientWrapper createAdminClient(Vertx vertx, Map acConfig, Promise prom) {
        AdminClientWrapper acw = new AdminClientWrapper(vertx, acConfig);
        try {
            acw.open();
            return acw;
        } catch (Exception e) {
            prom.fail(e);
            log.error(e);
            if (acw != null) {
                acw.close();
            }
            return null;
        }
    }
}
