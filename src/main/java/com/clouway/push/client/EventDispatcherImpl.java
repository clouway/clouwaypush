package com.clouway.push.client;

import com.clouway.push.shared.HandlerRegistration;
import com.clouway.push.shared.PushEvent;
import com.clouway.push.shared.PushEventHandler;
import com.google.common.collect.Lists;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Georgi Georgiev (GeorgievJon@gmail.com)
 */
public class EventDispatcherImpl implements EventDispatcher {

  private Map<String, List<PushEventHandler>> handlerMap = new HashMap<String, List<PushEventHandler>>();

  @Override
  public HandlerRegistration addHandler(PushEvent.Type type, final PushEventHandler handler) {
    final String key = type.getKey();

    if(handlerMap.containsKey(key)) {
      List<PushEventHandler> handlers = handlerMap.get(key);
      handlers.add(handler);
    } else {
      handlerMap.put(type.getKey(), Lists.newArrayList(handler));
    }

    return new HandlerRegistration() {
      @Override
      public void removeHandler() {
        if(handlerMap.containsKey(key)) {
          List<PushEventHandler> handlers = handlerMap.get(key);
          handlers.remove(handler);

          if(handlers.isEmpty()) {
            handlerMap.remove(key);
          }
        }
      }
    };
  }

  @Override
  public void fire(PushEvent event) {
    String key = event.getAssociatedType().getKey();

    if(handlerMap.containsKey(key)) {
      List<PushEventHandler> handlers = handlerMap.get(key);

      for (PushEventHandler handler : handlers) {
        event.dispatch(handler);
      }
    }
  }
}
