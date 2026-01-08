package org.tron.common.logsfilter.trigger;

import java.util.ArrayList;
import java.util.List;
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
        + ", transactionList: " + transactionList;
  }
}
