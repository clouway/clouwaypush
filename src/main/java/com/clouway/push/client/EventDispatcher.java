package com.clouway.push.client;

import com.clouway.push.shared.HandlerRegistration;
import com.clouway.push.shared.PushEvent;
import com.clouway.push.shared.PushEventHandler;

/**
 * @author Georgi Georgiev (GeorgievJon@gmail.com)
 */
public interface EventDispatcher {

  HandlerRegistration addHandler(PushEvent.Type type, PushEventHandler handler);

  void fire(PushEvent event);
}
