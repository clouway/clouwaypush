package com.clouway.push.server;

import com.clouway.push.shared.PushEvent;

import java.util.List;

/**
 * @author Ivan Lazov <ivan.lazov@clouway.com>
 */
public interface SubscriptionsRepository {

  boolean hasSubscription(PushEvent.Type eventType, String subscriber);

  void put(Subscription subscription);

  void removeSubscription(PushEvent.Type eventType, String subscriber);

  List<Subscription> findSubscriptions(String subscriber);

  List<Subscription> findSubscriptions(PushEvent.Type type);

  void removeSubscription(Subscription subscription);

  void removeAllSubscriptions(String subscriber);
}
