package com.clouway.push.client.channelapi;

import com.clouway.push.shared.PushEvent;
import com.google.gwt.user.client.rpc.RemoteService;

/**
 * @author Ivan Lazov <ivan.lazov@clouway.com>
 */
public interface PushChannelService extends RemoteService {

  String open();

  void subscribe(PushEvent.SerializableType type);

  void unsubscribe(PushEvent event);
}
