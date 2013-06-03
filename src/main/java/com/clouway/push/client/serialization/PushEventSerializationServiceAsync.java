package com.clouway.push.client.serialization;

import com.clouway.push.shared.PushEvent;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * Async interface for {@link PushEventSerializationService}. This class is not used directly by client code.
 *
 * @author Ivan Lazov <ivan.lazov@clouway.com>
 */
public interface PushEventSerializationServiceAsync {

  void receiveEvent(AsyncCallback<PushEvent> async);
}
