package com.clouway.push.client.channelapi;

import com.clouway.push.shared.PushEvent;
import com.google.gwt.user.client.rpc.RemoteService;

/**
 * @author Ivan Lazov <ivan.lazov@clouway.com>
 */
public interface PushChannelService extends RemoteService {

  String openChannel(String subscriber);

  void subscribe(String subscriber,PushEvent.Type type);

  void unsubscribe(String subscriber, PushEvent.Type event);

  void iAmAlive(String subscriber,int seconds);

  PushEvent dummyMethod();
}
