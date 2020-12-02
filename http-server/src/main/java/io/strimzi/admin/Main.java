package io.strimzi.admin;

import io.strimzi.admin.http.server.AdminServer;
import io.vertx.config.ConfigRetriever;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import org.apache.commons.cli.ParseException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;

import java.io.File;

public class Main {

    private static final Logger LOGGER = LogManager.getLogger(Main.class);

    /**
     * Main entrypoint.
     *
     * @param args the command line arguments
     */
    public static void main(final String[] args) throws ParseException {
        LOGGER.info("AdminServer is starting.");

        final Vertx vertx = Vertx.vertx();
        run(vertx, args)
            .onFailure(throwable -> {
                LOGGER.atFatal().withThrowable(throwable).log("AdminServer startup failed.");
                System.exit(1);
            });
    }

    static Future<String> run(final Vertx vertx, String[] args) throws ParseException {
        final Promise<String> promise = Promise.promise();

        parseConfig(vertx, args).getConfig(ar -> {
            if (ar.succeeded()) {
                final AdminServer adminServer = new AdminServer(ar.result().getMap());
                vertx.deployVerticle(adminServer,
                    res -> {
                        if (res.failed()) {
                            LOGGER.atFatal().withThrowable(res.cause()).log("AdminServer verticle failed to start");
                        }
                        promise.handle(res);
                    }
                );
            } else {
                promise.fail("Failed to parse configuration. Reason: " + ar.cause().getMessage());
            }
        });

        return promise.future();
    }

    private static ConfigRetriever parseConfig(Vertx vertx, String[] args) throws ParseException {
        CommandLine commandLine = new DefaultParser().parse(generateOptions(), args);

        ConfigStoreOptions fileStore = new ConfigStoreOptions()
                .setType("file")
                .setFormat("properties")
                .setConfig(new JsonObject().put("path", absoluteFilePath(commandLine.getOptionValue("config-file"))).put("raw-data", true));

        ConfigStoreOptions envStore = new ConfigStoreOptions()
                .setType("env")
                .setConfig(new JsonObject().put("raw-data", true));

        ConfigRetrieverOptions options = new ConfigRetrieverOptions()
                .addStore(fileStore)
                .addStore(envStore);

        ConfigRetriever retriever = ConfigRetriever.create(vertx, options);
        return retriever;

    }

    private static String absoluteFilePath(String arg) {
        // return the file path as absolute (if it's relative)
        return arg.startsWith(File.separator) ? arg : System.getProperty("user.dir") + File.separator + arg;
    }

    /**
     * Generate the command line options
     *
     * @return command line options
     */
    private static Options generateOptions() {

        Option configFileOption = Option.builder()
                .required(true)
                .hasArg(true)
                .longOpt("config-file")
                .desc("Configuration file with bridge parameters")
                .build();

        Options options = new Options();
        options.addOption(configFileOption);
        return options;
    }
}
