package com.clouway.push.client;

import com.clouway.push.shared.PushEvent;

/**
 * @author Ivan Lazov <ivan.lazov@clouway.com>
 */
public interface PushChannelApi {

  boolean hasOpennedChannel();

  void connect();

  void subscribe(PushEvent.Type type, AsyncSubscribeCallback callback);

  void addPushEventListener(PushEventListener listener);
}
