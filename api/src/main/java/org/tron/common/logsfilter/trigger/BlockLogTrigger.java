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
  private long latestSolidifiedBlockNumber;

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
  Map<String, Long> witnessMap = new HashMap<>();

  public BlockLogTrigger() {
    setTriggerName(Trigger.BLOCK_TRIGGER_NAME);
  }

  @Override
  public String toString() {
    return "triggerName: " + getTriggerName()
        + ", timestamp: " + timeStamp
        + ", blockNumber: " + blockNumber
        + ", blockhash: " + blockHash
        + ", transactionSize: " + transactionSize
        + ", latestSolidifiedBlockNumber: " + latestSolidifiedBlockNumber
        + ", transactionList: " + transactionList
        + ", witnessAddress: " + witnessAddress
        + ", witnessPayPerBlock: " + witnessPayPerBlock
        + ", witnessMap: " + witnessMap;
  }
}
