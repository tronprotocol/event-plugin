package org.tron.mongodb;

import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

public class BaseEntity implements Serializable {

  @Setter
  @Getter
  private Object _id;
}
