package com.clouway.push.client;

import com.clouway.push.client.channelapi.AsyncUnsubscribeCallBack;
import com.clouway.push.shared.PushEvent;

/**
 * @author Ivan Lazov <ivan.lazov@clouway.com>
 */
public interface PushChannelApi extends OnTimeCallBack {

  boolean hasInitialConnection();

  void connect();

  void subscribe(PushEvent.Type type, AsyncSubscribeCallback callback);

  void addPushEventListener(PushEventListener listener);

  void unsubscribe(PushEvent.Type type, AsyncUnsubscribeCallBack callBack);
}
