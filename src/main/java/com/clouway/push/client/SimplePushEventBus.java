package com.clouway.push.client;

import com.clouway.push.shared.PushEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Ivan Lazov <ivan.lazov@clouway.com>
 */
public class SimplePushEventBus implements PushEventBus {

  private final Map<String, List<PushEventHandler>> eventHandlers = new HashMap<String, List<PushEventHandler>>();

  @Override
  public void fireEvent(PushEvent pushEvent) {

    List<PushEventHandler> eventHandlerList = eventHandlers.get(pushEvent.getEventName());

    if (eventHandlerList == null) {
      throw new PushEventHandlerNotFoundException();
    }

    for (PushEventHandler eventHandler : eventHandlerList) {
      eventHandler.onEvent(pushEvent);
    }
  }

  @Override
  public void addHandler(PushEvent event, PushEventHandler eventHandler) {

    List<PushEventHandler> eventHandlerList = new ArrayList<PushEventHandler>();

    if (eventHandlers.containsKey(event.getEventName())) {
      eventHandlerList = eventHandlers.get(event.getEventName());
      eventHandlerList.add(eventHandler);
    } else {
      eventHandlerList.add(eventHandler);
    }

    eventHandlers.put(event.getEventName(), eventHandlerList);
  }

  @Override
  public void removeHandlers(PushEvent event) {
    eventHandlers.remove(event.getEventName());
  }

  @Override
  public void removeHandler(PushEvent event, PushEventHandler eventHandler) {
    eventHandlers.get(event.getEventName()).remove(eventHandler);
  }
}
