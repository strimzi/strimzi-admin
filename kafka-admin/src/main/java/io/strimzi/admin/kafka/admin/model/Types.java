package io.strimzi.admin.kafka.admin.model;

import java.util.List;

public class Types {
    public static class TopicOnlyName {
        private String name;
        public TopicOnlyName() {}
        public String getName() { return this.name; }
        public void setName(String name) { this.name = name; }
    }

    public static class Topic {
        private String name;
        private TopicConfig config;

        public Topic() {}

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
    }

    public static class QueryGetTopicArgs {
        private String name;

        public QueryGetTopicArgs() {}

        public String getName() { return this.name; }
        public void setName(String name) { this.name = name; }
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

        public void setreplicationFactor(Long replicationFactor) {
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

        public CreateTopicInput() {}

        public String getName() { return this.name; }
        public CreateOrMutateTopicConfigInput getConfig() { return this.config; }
        public void setName(String name) { this.name = name; }
        public void setConfig(CreateOrMutateTopicConfigInput config) { this.config = config; }
    }
    public static class MutationCreateTopicArgs {
        private CreateTopicInput input;

        public MutationCreateTopicArgs() {}

        public CreateTopicInput getInput() { return this.input; }
        public void setInput(CreateTopicInput input) { this.input = input; }
    }
    public static class MutationUpdateTopicArgs {
        private MutateTopicInput input;

        public MutationUpdateTopicArgs() {}

        public MutateTopicInput getInput() { return this.input; }
        public void setInput(MutateTopicInput input) { this.input = input; }
    }
    public static class MutationDeleteTopicArgs {
        private MutateTopicInput input;

        public MutationDeleteTopicArgs() {}

        public MutateTopicInput getInput() { return this.input; }
        public void setInput(MutateTopicInput input) { this.input = input; }
    }
    public static class MutateTopicInput {
        //private Object id;
        private String name;
        private CreateOrMutateTopicConfigInput config;

        public MutateTopicInput() {}

        //public Object getId() { return this.id; }
        public String getName() { return this.name; }
        public CreateOrMutateTopicConfigInput getConfig() { return this.config; }
        //public void setId(Object id) { this.id = id; }
        public void setName(String name) { this.name = name; }
        public void setConfig(CreateOrMutateTopicConfigInput config) { this.config = config; }
    }

    public static class Replicas {
        private Boolean in_sync;
        private String id;

        public Replicas() {
        }
        public Boolean getIn_sync() {
            return in_sync;
        }
        public void setIn_sync(Boolean in_sync) {
            this.in_sync = in_sync;
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

    public static class TopicConfig {
        private Long partitionCount;
        private Long replicationFactor;
        private Boolean isInternal;
        private List<Partitions> partitions;

        public TopicConfig() {
        }

        public Long getPartitionCount() {
            return partitionCount;
        }

        public void setpartitionCount(Long partitionCount) {
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

        public List<Partitions> getPartitions() {
            return partitions;
        }

        public void setPartitions(List<Partitions> partitions) {
            this.partitions = partitions;
        }
    }
}
