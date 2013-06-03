package com.clouway.push.client;

import com.clouway.push.shared.PushEvent;
import com.clouway.push.shared.PushEventHandler;

/**
 * @author Ivan Lazov <ivan.lazov@clouway.com>
 */
public interface PushEventBus {

  void addHandler(final PushEvent.Type type, final PushEventHandler handler);
}
