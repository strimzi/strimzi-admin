#!/usr/bin/env sh

java -cp "/opt/strimzi/libs/*" --module-path /opt/strimzi/modules/ --module io.strimzi.http.server/io.strimzi.http.server.Main
