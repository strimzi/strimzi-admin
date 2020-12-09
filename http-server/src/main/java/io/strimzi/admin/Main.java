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

        final AdminServer adminServer = new AdminServer();
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
}
