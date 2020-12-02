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
        private Integer retentionDays;
        private Integer partitionCount;
        private Integer minInSyncReplicas;
        private Integer replicationFactor;

        public CreateOrMutateTopicConfigInput() {}

        public Integer getRetentionDays() { return this.retentionDays; }
        public Integer getPartitionCount() { return this.partitionCount; }
        public Integer getMinInSyncReplicas() { return this.minInSyncReplicas; }
        public void setRetentionDays(Integer retentionDays) { this.retentionDays = retentionDays; }
        public void setPartitionCount(Integer partitionCount) { this.partitionCount = partitionCount; }
        public void setMinInSyncReplicas(Integer minInSyncReplicas) { this.minInSyncReplicas = minInSyncReplicas; }
        public Integer getReplicationFactor() { return replicationFactor; }
        public void setReplicationFactor(Integer replicationFactor) { this.replicationFactor = replicationFactor; }
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
        private Long partition_count;
        private Long replication_factor;
        private Boolean is_internal;
        private List<Partitions> partitions;

        public TopicConfig() {
        }

        public Long getPartition_count() {
            return partition_count;
        }

        public void setPartition_count(Long partition_count) {
            this.partition_count = partition_count;
        }

        public Long getReplication_factor() {
            return replication_factor;
        }

        public void setReplication_factor(Long replication_factor) {
            this.replication_factor = replication_factor;
        }

        public Boolean getIs_internal() {
            return is_internal;
        }

        public void setIs_internal(Boolean is_internal) {
            this.is_internal = is_internal;
        }

        public List<Partitions> getPartitions() {
            return partitions;
        }

        public void setPartitions(List<Partitions> partitions) {
            this.partitions = partitions;
        }
    }
}
