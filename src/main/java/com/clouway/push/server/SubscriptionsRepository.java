package com.clouway.push.server;

import com.clouway.push.shared.PushEvent;

/**
 * @author Ivan Lazov <ivan.lazov@clouway.com>
 */
public interface SubscriptionsRepository {

  boolean hasSubscription(PushEvent.Type eventType, String subscriber);

  void put(Subscription subscription);

  Subscription get(PushEvent.Type eventType, String subscriber);

  void removeSubscription(PushEvent.Type eventType, String subscriber);
}
