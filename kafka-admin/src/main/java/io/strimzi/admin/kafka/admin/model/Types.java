/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package io.strimzi.admin.kafka.admin.model;

import java.util.List;

public class Types {

    public static class Node {
        private Integer id;

        public Integer getId() {
            return id;
        }

        public void setId(Integer id) {
            this.id = id;
        }
    }

    public static class Partition {
        // ID
        private Integer partition;
        private List<Node> replicas;
        // InSyncReplicas
        private List<Node> isr;
        private Node leader;

        public Integer getPartition() {
            return partition;
        }

        public void setPartition(Integer partition) {
            this.partition = partition;
        }

        public List<Node> getReplicas() {
            return replicas;
        }

        public void setReplicas(List<Node> replicas) {
            this.replicas = replicas;
        }

        public List<Node> getIsr() {
            return isr;
        }

        public void setIsr(List<Node> isr) {
            this.isr = isr;
        }

        public Node getLeader() {
            return leader;
        }

        public void setLeader(Node leader) {
            this.leader = leader;
        }
    }

    public static class ConfigEntry {
        private String key;
        private String value;

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

    public static class Topic implements Comparable<Topic> {
        // ID
        private String name;
        private Boolean isInternal;
        private List<Partition> partitions;
        private List<ConfigEntry> config;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Boolean getIsInternal() {
            return isInternal;
        }

        public void setIsInternal(Boolean internal) {
            isInternal = internal;
        }

        public List<Partition> getPartitions() {
            return partitions;
        }

        public void setPartitions(List<Partition> partitions) {
            this.partitions = partitions;
        }

        public List<ConfigEntry> getConfig() {
            return config;
        }

        public void setConfig(List<ConfigEntry> config) {
            this.config = config;
        }

        @Override
        public int compareTo(Topic topic) {
            return getName().compareTo(topic.getName());
        }
    }

    public static class TopicList {
        private List<Topic> items;
        private Integer offset;
        private Integer limit;
        private Integer count;

        public List<Topic> getItems() {
            return items;
        }

        public void setItems(List<Topic> items) {
            this.items = items;
        }

        public Integer getOffset() {
            return offset;
        }

        public void setOffset(Integer offset) {
            this.offset = offset;
        }

        public Integer getLimit() {
            return limit;
        }

        public void setLimit(Integer limit) {
            this.limit = limit;
        }

        public Integer getCount() {
            return count;
        }

        public void setCount(Integer count) {
            this.count = count;
        }
    }

    public static class NewTopicConfigEntry {
        private String key;
        private String value;

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

    public static class NewTopicInput {
        private Integer numPartitions;
        private Integer replicationFactor;
        private List<NewTopicConfigEntry> config;

        public Integer getNumPartitions() {
            return numPartitions;
        }

        public void setNumPartitions(Integer numPartitions) {
            this.numPartitions = numPartitions;
        }

        public Integer getReplicationFactor() {
            return replicationFactor;
        }

        public void setReplicationFactor(Integer replicationFactor) {
            this.replicationFactor = replicationFactor;
        }

        public List<NewTopicConfigEntry> getConfig() {
            return config;
        }

        public void setConfig(List<NewTopicConfigEntry> config) {
            this.config = config;
        }
    }

    public static class NewTopic {
        private String name;
        private NewTopicInput settings;
        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public NewTopicInput getSettings() {
            return settings;
        }

        public void setSettings(NewTopicInput settings) {
            this.settings = settings;
        }
    }

    public static class UpdatedTopic {
        private String name;
        private List<NewTopicConfigEntry> config;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public List<NewTopicConfigEntry> getConfig() {
            return config;
        }

        public void setConfig(List<NewTopicConfigEntry> config) {
            this.config = config;
        }
    }

    public static class PageRequest {
        private Integer limit;
        private Integer offset;

        public Integer getLimit() {
            return limit;
        }

        public void setLimit(Integer limit) {
            this.limit = limit;
        }

        public Integer getOffset() {
            return offset;
        }

        public void setOffset(Integer offset) {
            this.offset = offset;
        }
    }
    enum SortDirectionEnum {
        DESC,
        ASC
    }

    public static class OrderByInput {
        private String field;
        private SortDirectionEnum order;

        public String getField() {
            return field;
        }

        public void setField(String field) {
            this.field = field;
        }

        public SortDirectionEnum getOrder() {
            return order;
        }

        public void setOrder(SortDirectionEnum order) {
            this.order = order;
        }
    }

    public static class ConsumerGroup {
        private String id;
        private Boolean simple;

        public Boolean getSimple() {
            return simple;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public void setSimple(Boolean simple) {
            this.simple = simple;
        }
    }

    public static class ConsumerGroupDescription extends ConsumerGroup {
        private Coordinator coordinator;

        public List<MemberDesc> getMembers() {
            return members;
        }

        public void setMembers(List<MemberDesc> members) {
            this.members = members;
        }

        private List<MemberDesc> members;

        public String getState() {
            return state;
        }

        public void setState(String state) {
            this.state = state;
        }

        private String state;

        public Coordinator getCoordinator() {
            return coordinator;
        }

        public void setCoordinator(Coordinator coordinator) {
            this.coordinator = coordinator;
        }
    }

    public static class MemberDesc {
        private String clientId;
        private String consumerId;
        private List<Integer> assignment;

        public String getClientId() {
            return clientId;
        }

        public void setClientId(String clientId) {
            this.clientId = clientId;
        }

        public String getConsumerId() {
            return consumerId;
        }

        public void setConsumerId(String consumerId) {
            this.consumerId = consumerId;
        }

        public List<Integer> getAssignment() {
            return assignment;
        }

        public void setAssignment(List<Integer> assignment) {
            this.assignment = assignment;
        }

        public String getHost() {
            return host;
        }

        public void setHost(String host) {
            this.host = host;
        }

        private String host;
    }

    public static class Coordinator {
        private boolean hasRack;
        private String host;
        private int id;
        private boolean isEmpty;
        private int port;
        private String rack;

        public boolean isHasRack() {
            return hasRack;
        }

        public void setHasRack(boolean hasRack) {
            this.hasRack = hasRack;
        }

        public String getHost() {
            return host;
        }

        public void setHost(String host) {
            this.host = host;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public boolean isEmpty() {
            return isEmpty;
        }

        public void setEmpty(boolean empty) {
            isEmpty = empty;
        }

        public int getPort() {
            return port;
        }

        public void setPort(int port) {
            this.port = port;
        }

        public String getRack() {
            return rack;
        }

        public void setRack(String rack) {
            this.rack = rack;
        }
    }
}
