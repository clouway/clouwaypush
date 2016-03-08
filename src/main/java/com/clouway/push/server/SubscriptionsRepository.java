package com.clouway.push.server;

import com.clouway.push.server.util.DateTime;

import java.util.List;
import java.util.Set;

/**
 * @author Ivan Lazov <ivan.lazov@clouway.com>
 */
public interface SubscriptionsRepository {

  void put(Subscription subscription);

  void put(String subscriber, List<Subscription> subscriptions);

  void removeSubscriptions(String eventKey, Set<String> subscribers);

  List<Subscription> findSubscriptions(String eventKey);

  void keepAliveTill(String subscriber, DateTime dateTime);
}
