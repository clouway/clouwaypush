package com.clouway.push.client.channelapi;

import com.clouway.push.shared.PushEvent;
import com.google.gwt.user.client.rpc.RemoteService;

import java.util.List;

/**
 * @author Ivan Lazov <ivan.lazov@clouway.com>
 */
public interface PushChannelService extends RemoteService {

  String connect(String subscriber);

  void subscribe(String subscriber, List<PushEvent.Type> types);

  void unsubscribe(String subscriber, PushEvent.Type event);

  void keepAlive(String subscriber);

  PushEvent dummyMethod();
}
