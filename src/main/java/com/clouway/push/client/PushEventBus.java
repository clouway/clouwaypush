package com.clouway.push.client;

import com.clouway.push.shared.PushEvent;

/**
 * @author Ivan Lazov <ivan.lazov@clouway.com>
 */
public interface PushEventBus {

  void fireEvent(PushEvent pushEvent);

  <T extends PushEvent> void addHandler(T event, PushEventHandler<T> eventHandler);

  void removeHandlers(PushEvent event);
}
