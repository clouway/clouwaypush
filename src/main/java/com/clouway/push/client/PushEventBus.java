package com.clouway.push.client;

import com.clouway.push.shared.PushEvent;
import com.clouway.push.shared.PushEventHandler;

/**
 * @author Ivan Lazov <ivan.lazov@clouway.com>
 */
public interface PushEventBus {

  void addHandler(final PushEvent.SerializableType type, final PushEventHandler handler);

//  void removeHandlers(PushEvent event);

//  void removeHandler(PushEvent event, PushEventHandler eventHandler);

}
