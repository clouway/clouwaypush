package com.clouway.push.client;

import com.clouway.push.shared.PushEvent;

/**
 * @author Ivan Lazov <ivan.lazov@clouway.com>
 */
public interface PushEventBus {

  void fireEvent(PushEvent pushEvent);

  void addHandler(PushEvent event, PushEventHandler eventHandler);

  void removeHandlers(PushEvent event);

  void removeHandler(PushEvent event, PushEventHandler eventHandler);
}
