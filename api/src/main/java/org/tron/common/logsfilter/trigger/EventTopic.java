package org.tron.common.logsfilter.trigger;


import lombok.Getter;

@Getter
public enum EventTopic {
  BLOCK_TRIGGER(0, "blockTrigger"),
  TRANSACTION_TRIGGER(1, "transactionTrigger"),
  CONTRACT_LOG_TRIGGER(2, "contractLogTrigger"),
  CONTRACT_EVENT_TRIGGER(3, "contractEventTrigger"),
  SOLIDITY_TRIGGER(4, "solidityTrigger"),
  SOLIDITY_EVENT(5, "solidityEventTrigger"),
  SOLIDITY_LOG(6, "solidityLogTrigger");

  private final Integer type;
  private final String name;

  EventTopic(Integer type, String name) {
    this.type = type;
    this.name = name;
  }

  public static EventTopic getEventTopicByType(int topicType) {
    for (EventTopic member : values()) {
      if (member.getType() == topicType) {
        return member;
      }
    }
    return null;
  }

  public static EventTopic getEventTopicByName(String topicName) {
    for (EventTopic member : values()) {
      if (member.getName().equals(topicName)) {
        return member;
      }
    }
    return null;
  }
}
