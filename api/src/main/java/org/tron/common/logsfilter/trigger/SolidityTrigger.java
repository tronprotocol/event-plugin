package org.tron.common.logsfilter.trigger;

import lombok.Getter;
import lombok.Setter;

public class SolidityTrigger extends Trigger {

  @Getter
  @Setter
  private long latestSolidifiedBlockNumber;

  @Override
  public String toString() {
    return "triggerName: " + getTriggerName()
        + ", timestamp: " + timeStamp
        + ", latestSolidifiedBlockNumber: " + latestSolidifiedBlockNumber;
  }

  public SolidityTrigger() {
    setTriggerName(Trigger.SOLIDITY_TRIGGER_NAME);
  }
}
