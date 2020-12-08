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

        if (envConfig.get("SSL_KEYSTORE_LOCATION") != null && envConfig.get("SSL_KEYSTORE_PASSWORD") != null) {
            adminClientConfig.put(Constants.SSL_KEYSTORE_LOCATION_CONFIG, envConfig.get("SSL_KEYSTORE_LOCATION").toString());
            adminClientConfig.put(Constants.SSL_KEYSTORE_PASSWORD_CONFIG, envConfig.get("SSL_KEYSTORE_PASSWORD").toString());
            adminClientConfig.put(Constants.SSL_KEYSTORE_TYPE_CONFIG, "PKCS12");
        }

        if (envConfig.get("SSL_TRUSTSTORE_LOCATION") != null && envConfig.get("SSL_TRUSTSTORE_PASSWORD") != null) {
            adminClientConfig.put(Constants.SECURITY_PROTOCOL_CONFIG, "SSL");
            adminClientConfig.put(Constants.SSL_TRUSTSTORE_LOCATION_CONFIG, envConfig.get("SSL_TRUSTSTORE_LOCATION").toString());
            adminClientConfig.put(Constants.SSL_TRUSTSTORE_PASSWORD_CONFIG, envConfig.get("SSL_TRUSTSTORE_PASSWORD").toString());
            adminClientConfig.put(Constants.SSL_TRUSTSTORE_TYPE_CONFIG, "PKCS12");
        }

        adminClientConfig.put(Constants.METADATA_MAX_AGE_CONFIG, "30000");
        adminClientConfig.put(Constants.REQUEST_TIMEOUT_MS_CONFIG, "10000");
        adminClientConfig.put(Constants.RETRIES_CONFIG, "3");
        adminClientConfig.put(Constants.DEFAULT_API_TIMEOUT_MS_CONFIG, "40000");


        return adminClientConfig;
    }
}
