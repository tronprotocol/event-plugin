package org.tron.common.logsfilter.trigger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;

public class BlockLogTrigger extends Trigger {
  @Getter
  @Setter
  private long blockNumber;

  @Getter
  @Setter
  private String blockHash;

  @Getter
  @Setter
  private long transactionSize;

  @Getter
  @Setter
  private List<String> transactionList = new ArrayList<>();


  /**
   * address of witness
   */
  @Getter
  @Setter
  private String witnessAddress;

  @Getter
  @Setter
  private long witnessPayPerBlock;

  @Getter
  @Setter
  private String parentHash;

  @Getter
  @Setter
  Map<String, Long> witnessMap = new HashMap<>();

  public BlockLogTrigger() {
    setTriggerName(Trigger.BLOCK_TRIGGER_NAME);
  }

  @Override
  public String toString() {
    return new StringBuilder().append("triggerName: ").append(getTriggerName())
      .append("timestamp: ")
      .append(timeStamp)
      .append(", blockNumber: ")
      .append(blockNumber)
      .append(", blockhash: ")
      .append(blockHash)
      .append(", transactionSize: ")
      .append(transactionSize)
      .append(", transactionList: ")
      .append(transactionList)
      .append(", witnessAddress: ")
      .append(witnessAddress)
      .append(", witnessPayPerBlock: ")
      .append(witnessPayPerBlock)
      .append(", witnessMap: ")
      .append(witnessMap)
      .append(", parentHash: ")
      .append(parentHash)
      .toString();
  }
}
