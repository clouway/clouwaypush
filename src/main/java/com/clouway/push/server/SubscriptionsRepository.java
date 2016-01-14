package com.clouway.push.server;

import com.clouway.push.shared.PushEvent;
import com.clouway.push.shared.util.DateTime;

import java.util.List;
import java.util.Set;

/**
 * @author Ivan Lazov <ivan.lazov@clouway.com>
 */
public interface SubscriptionsRepository {

  void put(Subscription subscription);

  void put(String subscriber, List<Subscription> subscriptions);

  void removeSubscriptions(PushEvent.Type type, Set<String> subscribers);

  List<Subscription> findSubscriptions(PushEvent.Type type);

  void keepAliveTill(String subscriber, DateTime dateTime);
}
