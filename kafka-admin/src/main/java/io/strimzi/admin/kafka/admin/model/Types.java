/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package io.strimzi.admin.kafka.admin.model;

import java.util.List;

public class Types {
    public static class TopicOnlyName {
        private String name;
        public TopicOnlyName() {}
        public String getName() {
            return this.name;
        }
        public void setName(String name) {
            this.name = name;
        }
    }

    public static class TopicDescription {
        private String name;
        private List<Partitions> partitions;
        private TopicConfig config;

        public TopicDescription() {}

        public String getName() {
            return this.name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public TopicConfig getConfig() {
            return config;
        }

        public void setConfig(TopicConfig config) {
            this.config = config;
        }

        public List<Partitions> getPartitions() {
            return partitions;
        }

        public void setPartitions(List<Partitions> partitions) {
            this.partitions = partitions;
        }
    }

    public static class CreateOrMutateTopicConfigInput {
        private Long partitionCount;
        private Long replicationFactor;
        private Boolean isInternal;

        private Integer minInSyncReplicas;
        private Integer retentionDays;

        public CreateOrMutateTopicConfigInput() {}

        public Long getPartitionCount() {
            return partitionCount;
        }

        public void setPartitionCount(Long partitionCount) {
            this.partitionCount = partitionCount;
        }

        public Long getReplicationFactor() {
            return replicationFactor;
        }

        public void setReplicationFactor(Long replicationFactor) {
            this.replicationFactor = replicationFactor;
        }

        public Boolean getIsInternal() {
            return isInternal;
        }

        public void setIsInternal(Boolean isInternal) {
            this.isInternal = isInternal;
        }

        public Integer getMinInSyncReplicas() {
            return minInSyncReplicas;
        }

        public void setMinInSyncReplicas(Integer minInSyncReplicas) {
            this.minInSyncReplicas = minInSyncReplicas;
        }

        public Integer getRetentionDays() {
            return retentionDays;
        }

        public void setRetentionDays(Integer retentionDays) {
            this.retentionDays = retentionDays;
        }
    }
    public static class CreateTopicInput {
        private String name;
        private CreateOrMutateTopicConfigInput config;

        public String getName() {
            return this.name;
        }
        public CreateOrMutateTopicConfigInput getConfig() {
            return this.config;
        }
        public void setName(String name) {
            this.name = name;
        }
        public void setConfig(CreateOrMutateTopicConfigInput config) {
            this.config = config;
        }
    }
    public static class MutationCreateTopicArgs {
        private CreateTopicInput input;

        public MutationCreateTopicArgs() {}

        public CreateTopicInput getInput() {
            return this.input;
        }
        public void setInput(CreateTopicInput input) {
            this.input = input;
        }
    }
    public static class MutationUpdateTopicArgs {
        private MutateTopicInput input;

        public MutationUpdateTopicArgs() {}

        public MutateTopicInput getInput() {
            return this.input;
        }
        public void setInput(MutateTopicInput input) {
            this.input = input;
        }
    }
    public static class MutationDeleteTopicArgs {
        private MutateTopicInput input;

        public MutationDeleteTopicArgs() {}

        public MutateTopicInput getInput() {
            return this.input;
        }
        public void setInput(MutateTopicInput input) {
            this.input = input;
        }
    }
    public static class MutateTopicInput {
        private String name;
        private CreateOrMutateTopicConfigInput config;

        public MutateTopicInput() {}

        public String getName() {
            return this.name;
        }
        public CreateOrMutateTopicConfigInput getConfig() {
            return this.config;
        }
        public void setName(String name) {
            this.name = name;
        }
        public void setConfig(CreateOrMutateTopicConfigInput config) {
            this.config = config;
        }
    }

    public static class Replicas {
        private Boolean inSync;
        private String id;

        public Replicas() {
        }
        public Boolean getInSync() {
            return inSync;
        }
        public void setInSync(Boolean inSync) {
            this.inSync = inSync;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }
    }

    public static class Partitions {
        private Integer partition;
        private List<Replicas> replicas;

        public Partitions() {
        }
        public Integer getPartition() {
            return partition;
        }

        public void setPartition(Integer partition) {
            this.partition = partition;
        }

        public List<Replicas> getReplicas() {
            return replicas;
        }

        public void setReplicas(List<Replicas> replicas) {
            this.replicas = replicas;
        }
    }

    public static class TopicConfigEntry {
        String key;
        String value;

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }

    public static class TopicConfig {
        private Long partitionCount;
        private Long replicationFactor;
        private Long minInsyncReplicas;
        private Boolean isInternal;
        private List<TopicConfigEntry> pairs;

        public Long getPartitionCount() {
            return partitionCount;
        }

        public void setPartitionCount(Long partitionCount) {
            this.partitionCount = partitionCount;
        }

        public Long getReplicationFactor() {
            return replicationFactor;
        }

        public void setReplicationFactor(Long replicationFactor) {
            this.replicationFactor = replicationFactor;
        }

        public Long getMinInsyncReplicas() {
            return minInsyncReplicas;
        }

        public void setMinInsyncReplicas(Long minInsyncReplicas) {
            this.minInsyncReplicas = minInsyncReplicas;
        }

        public Boolean getIsInternal() {
            return isInternal;
        }

        public void setIsInternal(Boolean internal) {
            isInternal = internal;
        }

        public List<TopicConfigEntry> getPairs() {
            return pairs;
        }

        public void setPairs(List<TopicConfigEntry> pairs) {
            this.pairs = pairs;
        }
    }
}
