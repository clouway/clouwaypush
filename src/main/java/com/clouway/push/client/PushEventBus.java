package com.clouway.push.client;

import com.clouway.push.shared.PushEvent;
import com.clouway.push.shared.PushEventHandler;
import com.google.web.bindery.event.shared.HandlerRegistration;

/**
 * @author Ivan Lazov <ivan.lazov@clouway.com>
 */
public interface PushEventBus {

  HandlerRegistration addHandler(final PushEvent.Type type, final PushEventHandler handler);
}
