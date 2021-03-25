/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package io.strimzi.admin.kafka.admin;

import io.strimzi.admin.kafka.admin.handlers.CommonHandler;
import io.strimzi.admin.kafka.admin.model.Types;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.kafka.admin.ConsumerGroupListing;
import io.vertx.kafka.admin.KafkaAdminClient;
import org.apache.kafka.common.errors.InvalidRequestException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ConsumerGroupOperations {
    protected static final Logger log = LogManager.getLogger(ConsumerGroupOperations.class);


    public static void getGroupList(KafkaAdminClient ac, Promise prom, Pattern pattern, int offset, final int limit) {
        Promise<List<ConsumerGroupListing>> listConsumerGroupsFuture = Promise.promise();
        Promise<Map<String, io.vertx.kafka.admin.ConsumerGroupDescription>> describeConsumerGroupsFuture = Promise.promise();

        ac.listConsumerGroups(listConsumerGroupsFuture);
        listConsumerGroupsFuture.future()
            .compose(groups -> {
                List<ConsumerGroupListing> filteredList = groups.stream().filter(groupId -> CommonHandler.byName(pattern, prom).test(groupId.getGroupId())).collect(Collectors.toList());

                List<Types.ConsumerGroup> mappedList = filteredList.stream().map(item -> {
                    Types.ConsumerGroup consumerGroup = new Types.ConsumerGroup();
                    consumerGroup.setId(item.getGroupId());
                    consumerGroup.setSimple(item.isSimpleConsumerGroup());
                    return consumerGroup;
                }).collect(Collectors.toList());
                return Future.succeededFuture(mappedList);
            })
            .compose(list -> {
                ac.describeConsumerGroups(list.stream().map(l -> l.getId()).collect(Collectors.toList()), describeConsumerGroupsFuture);
                return describeConsumerGroupsFuture.future();
            })
            .compose(descriptions -> {
                List<Types.ConsumerGroupDescription> list = getGroupsDescriptions(descriptions);
                list.sort(new CommonHandler.ConsumerGroupComparator());

                if (offset > list.size()) {
                    return Future.failedFuture(new InvalidRequestException("Offset (" + offset + ") cannot be greater than consumer group list size (" + list.size() + ")"));
                }
                int tmpLimit = limit;
                if (tmpLimit == 0) {
                    tmpLimit = list.size();
                }

                List<Types.ConsumerGroupDescription> croppedList = list.subList(offset, Math.min(offset + tmpLimit, list.size()));
                return Future.succeededFuture(croppedList);
            })
            .onComplete(finalRes -> {
                if (finalRes.failed()) {
                    prom.fail(finalRes.cause());
                } else {
                    prom.complete(finalRes.result());
                }
                ac.close();
            });
    }

    public static void deleteGroup(KafkaAdminClient ac, List<String> groupsToDelete, Promise prom) {
        ac.deleteConsumerGroups(groupsToDelete, res -> {
            if (res.failed()) {
                log.error(res.cause());
                prom.fail(res.cause());
            } else {
                prom.complete(groupsToDelete);
            }
            ac.close();
        });
    }

    public static void describeGroup(KafkaAdminClient ac, Promise prom, List<String> groupToDescribe) {
        ac.describeConsumerGroups(groupToDescribe, res -> {
            if (res.failed()) {
                log.error(res.cause());
                prom.fail(res.cause());
            } else {
                Types.ConsumerGroupDescription groupDescription = getGroupsDescriptions(res.result()).get(0);
                prom.complete(groupDescription);
            }
            ac.close();
        });
    }

    private static List<Types.ConsumerGroupDescription> getGroupsDescriptions(Map<String, io.vertx.kafka.admin.ConsumerGroupDescription> asyncResult) {
        return asyncResult.entrySet().stream().map(group -> {
            Types.ConsumerGroupDescription grp = new Types.ConsumerGroupDescription();
            grp.setId(group.getValue().getGroupId());
            grp.setSimple(group.getValue().isSimpleConsumerGroup());

            Types.Coordinator coordinator = new Types.Coordinator();
            coordinator.setId(group.getValue().getCoordinator().getId());
            coordinator.setEmpty(group.getValue().getCoordinator().isEmpty());
            coordinator.setHasRack(group.getValue().getCoordinator().hasRack());
            coordinator.setHost(group.getValue().getCoordinator().getHost());
            coordinator.setPort(group.getValue().getCoordinator().getPort());
            coordinator.setRack(group.getValue().getCoordinator().rack());
            grp.setCoordinator(coordinator);
            grp.setState(group.getValue().getState().name());

            List<Types.MemberDesc> members = group.getValue().getMembers().stream().map(mem -> {
                Types.MemberDesc member = new Types.MemberDesc();
                member.setClientId(mem.getClientId());
                member.setConsumerId(mem.getConsumerId());
                member.setHost(mem.getHost());
                member.setAssignment(mem.getAssignment().getTopicPartitions().stream().map(part -> part.getPartition()).collect(Collectors.toList()));
                return member;
            }).collect(Collectors.toList());
            grp.setMembers(members);
            return grp;
        }).collect(Collectors.toList());
    }
}
