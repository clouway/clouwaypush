package com.clouway.push.client;

import com.clouway.push.shared.PushEvent;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * Async interface for {@link DefaultService}. This class is not used directly by client code.
 *
 * @author Ivan Lazov <ivan.lazov@clouway.com>
 */
public interface DefaultServiceAsync {

  void receiveEvent(AsyncCallback<PushEvent> async);
}
