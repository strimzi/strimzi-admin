/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package io.strimzi.admin;

import io.strimzi.admin.http.server.AdminServer;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

public class Main {

    private static final Logger LOGGER = LogManager.getLogger(Main.class);

    /**
     * Main entrypoint.
     *
     * @param args the command line arguments
     */
    public static void main(final String[] args) throws Exception {
        LOGGER.info("AdminServer is starting.");

        final Vertx vertx = Vertx.vertx();
        run(vertx)
            .onFailure(throwable -> {
                LOGGER.atFatal().withThrowable(throwable).log("AdminServer startup failed.");
                System.exit(1);
            });
    }

    static Future<String> run(final Vertx vertx) throws Exception {
        final Promise<String> promise = Promise.promise();

        final AdminServer adminServer = new AdminServer(envVarsToAdminClientConfig());
        vertx.deployVerticle(adminServer,
            res -> {
                if (res.failed()) {
                    LOGGER.atFatal().withThrowable(res.cause()).log("AdminServer verticle failed to start");
                }
                promise.handle(res);
            }
        );

        return promise.future();
    }

    private static Map envVarsToAdminClientConfig() throws Exception {
        Map envConfig = System.getenv();

        Map<String, String> adminClientConfig = new HashMap();
        if (envConfig.get("BOOTSTRAP_SERVERS") == null) {
            throw new Exception("Bootstrap address has to be specified");
        }
        adminClientConfig.put(Constants.BOOTSTRAP_SERVERS_CONFIG, envConfig.get("BOOTSTRAP_SERVERS").toString());

        // oAuth
        /*
        adminClientConfig.put(Constants.SECURITY_PROTOCOL_CONFIG, "SASL_SSL");
        adminClientConfig.put(Constants.SECURITY_SASL_MECHANISM, "OAUTHBEARER");
        adminClientConfig.put(Constants.SECURITY_SASL_JAAS_CONFIG, "org.apache.kafka.common.security.oauthbearer.OAuthBearerLoginModule required ;");
        adminClientConfig.put(Constants.SECURITY_SASL_LOGIN_CLASS, "io.strimzi.kafka.oauth.client.JaasClientOauthLoginCallbackHandler");
         */

        // admin client
        adminClientConfig.put(Constants.METADATA_MAX_AGE_CONFIG, "30000");
        adminClientConfig.put(Constants.REQUEST_TIMEOUT_MS_CONFIG, "10000");
        adminClientConfig.put(Constants.RETRIES_CONFIG, "3");
        adminClientConfig.put(Constants.DEFAULT_API_TIMEOUT_MS_CONFIG, "40000");

        return adminClientConfig;
    }
}
