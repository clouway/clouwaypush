package com.clouway.push.server;

import java.util.List;

/**
 * @author Ivan Lazov <ivan.lazov@clouway.com>
 */
public interface SubscriptionsRepository {

  void subscribe(String subscriber, String eventName);

  List<String> getSubscribedUsers(String eventName);
}
