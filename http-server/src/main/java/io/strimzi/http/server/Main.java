package io.strimzi.http.server;

import io.strimzi.http.server.logging.Utils;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {

    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    /**
     * Main entrypoint.
     *
     * @param args the command line arguments
     */
    public static void main(final String[] args) {

        LOGGER.info("AdminServer is starting.");

        final Vertx vertx = Vertx.vertx();

        run(vertx)
            .onFailure(throwable -> {
                LOGGER.error("AdminServer startup failed.");
                LOGGER.error(Utils.formatStacktrace(throwable));
                System.exit(1);
            });
    }

    static Future<String> run(final Vertx vertx) {

        final Promise<String> promise = Promise.promise();
        final AdminServer adminServer = new AdminServer();

        vertx.deployVerticle(adminServer,
            res -> {
                if (res.failed()) {
                    LOGGER.error("AdminServer verticle failed to start");
                    LOGGER.error(Utils.formatStacktrace(res.cause()));
                }
                promise.handle(res);
            }
        );

        return promise.future();
    }

}
