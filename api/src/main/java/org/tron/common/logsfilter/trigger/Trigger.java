package org.tron.common.logsfilter.trigger;

import lombok.Getter;
import lombok.Setter;

public abstract class Trigger {

  @Getter
  @Setter
  protected long timeStamp;

  @Getter
  @Setter
  private String triggerName;
}
