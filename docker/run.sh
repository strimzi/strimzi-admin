#!/usr/bin/env sh

java -cp ./kafka-admin-${STRIMZI_ADMIN_VERSION}-fat.jar:./health-${STRIMZI_ADMIN_VERSION}-fat.jar:./graphql-${STRIMZI_ADMIN_VERSION}-fat.jar:./http-server-${STRIMZI_ADMIN_VERSION}-fat.jar io.strimzi.admin.Main -XX:+ExitOnOutOfMemoryError