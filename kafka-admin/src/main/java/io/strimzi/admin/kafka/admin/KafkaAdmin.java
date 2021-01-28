/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package io.strimzi.admin.kafka.admin;

import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.config.SaslConfigs;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * KafkaAdmin class
 */
public class KafkaAdmin {

    protected final Logger log = LogManager.getLogger(KafkaAdmin.class);
    private static final String PREFIX = "KAFKA_ADMIN_";
    private static Map<String, Object> config;

    public KafkaAdmin() throws Exception {
        config = envVarsToAdminClientConfig(PREFIX);
        logConfiguration();
    }

    private Map envVarsToAdminClientConfig(String prefix) throws Exception {
        Map envConfig = System.getenv().entrySet().stream().filter(entry -> entry.getKey().startsWith(prefix)).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        Map<String, String> adminClientConfig = new HashMap();
        if (envConfig.get(PREFIX + "BOOTSTRAP_SERVERS") == null) {
            throw new Exception("Bootstrap address has to be specified");
        }
        adminClientConfig.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, envConfig.get(PREFIX + "BOOTSTRAP_SERVERS").toString());

        // oAuth
        if (System.getenv(PREFIX + "OAUTH_ENABLED") == null ? true : Boolean.valueOf(System.getenv(PREFIX + "OAUTH_ENABLED"))) {
            log.info("oAuth enabled");
            adminClientConfig.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SASL_PLAINTEXT");
            adminClientConfig.put(SaslConfigs.SASL_MECHANISM, "OAUTHBEARER");
            adminClientConfig.put(SaslConfigs.SASL_LOGIN_CALLBACK_HANDLER_CLASS, "io.strimzi.kafka.oauth.client.JaasClientOauthLoginCallbackHandler");
        } else {
            log.info("oAuth disabled");
        }
        // admin client
        adminClientConfig.put(AdminClientConfig.METADATA_MAX_AGE_CONFIG, "30000");
        adminClientConfig.put(AdminClientConfig.REQUEST_TIMEOUT_MS_CONFIG, "10000");
        adminClientConfig.put(AdminClientConfig.DEFAULT_API_TIMEOUT_MS_CONFIG, "30000");

        return adminClientConfig;
    }

    private void logConfiguration() {
        log.info("AdminClient configuration:");
        config.entrySet().forEach(entry -> {
            log.info("\t{} = {}", entry.getKey(), entry.getValue());
        });
    }

    public Map getAcConfig() {
        return config;
    }
}

