package com.clouway.push.server;

import com.clouway.push.server.util.DateTime;

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

  public static class Builder {

    private String subscriber;

    private DateTime expirationDateAndTime;
    private String eventKey;

    public Builder eventKey(String eventKey) {
      this.eventKey = eventKey;
      return this;
    }

    public Builder subscriber(String subscriber) {
      this.subscriber = subscriber;
      return this;
    }

    public Builder expires(DateTime expirationDateAndTime) {
      this.expirationDateAndTime = expirationDateAndTime;
      return this;
    }

    public Subscription build() {

      Subscription subscription = new Subscription();

      subscription.eventKey = eventKey;
      subscription.subscriber = subscriber;
      subscription.expirationDate = expirationDateAndTime;

      return subscription;
    }
  }


  private String eventKey;

  private String subscriber;

  private DateTime expirationDate;

  public String getEventKey() {
    return eventKey;
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
