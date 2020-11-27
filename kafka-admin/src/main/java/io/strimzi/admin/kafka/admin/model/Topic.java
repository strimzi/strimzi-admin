package io.strimzi.admin.kafka.admin.model;

public class Topic {
    private final String name;
    private final boolean isInternal;
    private final int partitionCount;
    private final int replicationFactor;

    private Topic(final String name, final boolean isInternal, final int partitionCount, final int replicationFactor) {
        this.name = name;
        this.isInternal = isInternal;
        this.partitionCount = partitionCount;
        this.replicationFactor = replicationFactor;
    }

    public static Topic create(final String name, final boolean isInternal, final int partitionCount, final int replicationFactor) {
        return new Topic(name, isInternal, partitionCount, replicationFactor);
    }

    public String getName() {
        return name;
    }

    public boolean isInternal() {
        return isInternal;
    }

    public int getPartitionCount() {
        return partitionCount;
    }

    public int getReplicationFactor() {
        return replicationFactor;
    }
}
