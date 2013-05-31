package com.clouway.push.server;

import com.clouway.push.shared.PushEvent;

import java.util.List;

/**
 * @author Ivan Lazov <ivan.lazov@clouway.com>
 */
public interface SubscriptionsRepository {

  void subscribe(String subscriber, PushEvent.SerializableType type);

  List<String> getSubscribedUsers(PushEvent.SerializableType type);
}
