package com.clouway.push.server;

import com.clouway.push.shared.PushEvent;
import com.clouway.push.shared.util.DateTime;

import java.io.Serializable;

/**
 * @author Ivan Lazov <ivan.lazov@clouway.com>
 */
public class Subscription implements Serializable {

  public static Builder aNewSubscription() {
    return new Builder();
  }

  public boolean isActive(DateTime dateTime) {
    return dateTime.isBefore(expirationDate);
  }

  public PushEvent.Type getEventType() {
    return eventType;
  }

  public static class Builder {

    private String eventName;

    private String subscriber;

    private DateTime expirationDateAndTime;

    private PushEvent.Type type;

    public Builder eventName(String eventName) {
      this.eventName = eventName;
      return this;
    }

    public Builder eventType(PushEvent.Type type) {
      this.type = type;
      eventName = type.getKey();
      return this;
    }

    public Builder subscriber(String subscriber) {
      this.subscriber = subscriber;
      return this;
    }

    public Builder expirationDate(DateTime expirationDateAndTime) {
      this.expirationDateAndTime = expirationDateAndTime;
      return this;
    }

    public Subscription build() {

      Subscription subscription = new Subscription();

      subscription.eventName = eventName;
      subscription.subscriber = subscriber;
      subscription.expirationDate = expirationDateAndTime;
      subscription.eventType = type;

      return subscription;
    }
  }

  private PushEvent.Type eventType;

  private String eventName;

  private String subscriber;

  private DateTime expirationDate;

  public String getEventName() {
    return eventName;
  }

  public String getSubscriber() {
    return subscriber;
  }

  public void setExpirationDate(DateTime expirationDate) {
    this.expirationDate = expirationDate;
  }

  public DateTime getExpirationDate() {
    return expirationDate;
  }

  public void renewingTillDate(DateTime dateTime) {
    expirationDate = dateTime;
  }
}
