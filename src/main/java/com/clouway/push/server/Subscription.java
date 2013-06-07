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
    if (dateTime.isBefore(expirationDate)) {
      return true;
    }
    return false;
  }

  public PushEvent.Type getEventType() {
    return eventType;
  }

  public static class Builder {

    private String eventName;

    private String subscriber;

    private DateTime expirationDateAndTime;
    private Integer timesSubscribed = 0;
    private PushEvent.Type type;


    public Builder eventName(String eventName) {
      this.eventName = eventName;
      return this;
    }

    public Builder eventType(PushEvent.Type type) {
      this.type = type;
      return this;
    }

    public Builder subscriber(String subscriber) {
      this.subscriber = subscriber;
      return this;
    }

    public Builder expirationDateAndTime(DateTime expirationDateAndTime) {
      this.expirationDateAndTime = expirationDateAndTime;
      return this;
    }

    public Builder timesSubscribed(Integer timesSubscribed) {
      this.timesSubscribed = timesSubscribed;
      return this;
    }

    public Subscription build() {

      Subscription subscription = new Subscription();

      subscription.eventName = eventName;
      subscription.subscriber = subscriber;
      subscription.expirationDate = expirationDateAndTime;
      subscription.timesSubscribed = timesSubscribed;
      subscription.eventType = type;

      return subscription;
    }

  }

  private PushEvent.Type eventType;

  private String eventName;

  private String subscriber;
  private DateTime expirationDate;
  private Integer timesSubscribed = 0;

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

  public void setTimesSubscribed(Integer timesSubscribed) {
    this.timesSubscribed = timesSubscribed;
  }

  public Integer getTimesSubscribed() {
    return timesSubscribed;
  }

  public void renewingTillDate(DateTime dateTime) {
    expirationDate = dateTime;
  }
}
