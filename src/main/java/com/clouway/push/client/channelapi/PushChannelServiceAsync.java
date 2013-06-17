package com.clouway.push.client.channelapi;

import com.clouway.push.shared.PushEvent;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * @author Ivan Lazov <ivan.lazov@clouway.com>
 */
public interface PushChannelServiceAsync {

  void connect(String subscriber, AsyncCallback<String> async);

  void subscribe(String subscriber, PushEvent.Type type, AsyncCallback<Void> async);

  void unsubscribe(String subscriber, PushEvent.Type event, AsyncCallback<Void> async);

  void keepAlive(String subscriber, AsyncCallback<Void> async);

  void dummyMethod(AsyncCallback<PushEvent> async);
}
