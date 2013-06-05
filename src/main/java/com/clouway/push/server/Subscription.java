package com.clouway.push.server;

import com.google.appengine.repackaged.org.joda.time.DateTime;

/**
 * @author Ivan Lazov <ivan.lazov@clouway.com>
 */
public class Subscription {

  public static Builder aNewSubscription() {
    return new Builder();
  }

  public static class Builder {

    private String eventName;
    private String subscriber;
    private DateTime expirationDateAndTime;
    private Integer timesSubscribed = 0;

    public Builder eventName(String eventName) {
      this.eventName = eventName;
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
      subscription.expirationDateAndTime = expirationDateAndTime;
      subscription.timesSubscribed = timesSubscribed;

      return subscription;
    }
  }

  private String eventName;
  private String subscriber;
  private DateTime expirationDateAndTime;
  private Integer timesSubscribed = 0;

  public String getEventName() {
    return eventName;
  }

  public String getSubscriber() {
    return subscriber;
  }

  public void setExpirationDateAndTime(DateTime expirationDateAndTime) {
    this.expirationDateAndTime = expirationDateAndTime;
  }

  public DateTime getExpirationDateAndTime() {
    return expirationDateAndTime;
  }

  public void setTimesSubscribed(Integer timesSubscribed) {
    this.timesSubscribed = timesSubscribed;
  }

  public Integer getTimesSubscribed() {
    return timesSubscribed;
  }
}
