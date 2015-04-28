package com.clouway.push.shared;

import java.io.Serializable;

/**
 * @author Ivan Lazov <ivan.lazov@clouway.com>
 */
public abstract class PushEvent<T extends PushEventHandler>  implements Serializable {

  protected PushEvent() {
  }

  public abstract Type<T> getAssociatedType();

  public abstract void dispatch(T handler);

  public static class Type<T> implements Serializable {

    private String eventName;
    private String correlationId = "";

    public Type() {
    }

    public Type(String eventName) {
      this.eventName = eventName;
    }

    public Type(String eventName, String correlationId) {
      this.eventName = eventName;
      this.correlationId = correlationId;
    }

    public String getKey() {
      return eventName + correlationId;
    }

    public void setCorrelationId(String correlationId) {
      if(correlationId == null) {
        correlationId = "";
      }
      this.correlationId = correlationId;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;

      Type type = (Type) o;

      if (correlationId != null ? !correlationId.equals(type.correlationId) : type.correlationId != null) return false;
      if (eventName != null ? !eventName.equals(type.eventName) : type.eventName != null) return false;

      return true;
    }

    @Override
    public int hashCode() {
      int result = eventName != null ? eventName.hashCode() : 0;
      result = 31 * result + (correlationId != null ? correlationId.hashCode() : 0);
      return result;
    }
  }
}
