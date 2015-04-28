package com.clouway.push.client;

import com.clouway.push.shared.HandlerRegistration;
import com.clouway.push.shared.PushEvent;
import com.clouway.push.shared.PushEventHandler;

/**
 * @author Ivan Lazov <ivan.lazov@clouway.com>
 */
public interface PushEventBus {

  HandlerRegistration addHandler(final PushEvent.Type type, final PushEventHandler handler);

  HandlerRegistration addHandler(final PushEvent.Type type,final String correlationId, final PushEventHandler handler);

  void fireEvent(PushEvent event);
}
