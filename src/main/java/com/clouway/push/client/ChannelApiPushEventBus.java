package com.clouway.push.client;

import com.clouway.push.client.channelapi.AsyncUnsubscribeCallBack;
import com.clouway.push.shared.HandlerRegistration;
import com.clouway.push.shared.PushEvent;
import com.clouway.push.shared.PushEventHandler;
import com.google.common.base.Strings;
import com.google.inject.Inject;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Ivan Lazov <ivan.lazov@clouway.com>
 */
public class ChannelApiPushEventBus implements PushEventBus {

  private final EventDispatcher eventDispatcher;
  private final PushChannelApi pushChannelApi;
  private final Map<String, Integer> eventsMap = new HashMap<String, Integer>();

  @Inject
  public ChannelApiPushEventBus(EventDispatcher eventDispatcher, PushChannelApi pushChannelApi) {
    this.eventDispatcher = eventDispatcher;
    this.pushChannelApi = pushChannelApi;

    pushChannelApi.addPushEventListener(new PushEventListener() {

      @Override
      public void onPushEvent(PushEvent event) {
         fireEvent(event);
      }
    });
  }

  @Override
  public HandlerRegistration addHandler(final PushEvent.Type type, final PushEventHandler handler) {
    return addHandler(type,"", handler);
  }

  @Override
  public HandlerRegistration addHandler(final PushEvent.Type eventType, String correlationId, PushEventHandler handler) {

    //transforming the default event type  using the correlationId
    if(!Strings.isNullOrEmpty(correlationId)) {
      eventType.setCorrelationId(correlationId);
    }

    final HandlerRegistration[] handlerRegistration = {null};

    pushChannelApi.connect();

    //subscription for event is independent from opening of channel
    subscribeForEvent(eventType, handlerRegistration, handler);


    return new HandlerRegistration() {

      public void removeHandler() {

        if (eventsMap.containsKey(eventType.getKey())) {

          eventsMap.put(eventType.getKey(), eventsMap.get(eventType.getKey()) - 1);

          if (eventsMap.get(eventType.getKey()) == 0) {

            eventsMap.remove(eventType.getKey());

            pushChannelApi.unsubscribe(eventType, new AsyncUnsubscribeCallBack() {

              public void onSuccess() {

                if (handlerRegistration[0] != null) {
                  handlerRegistration[0].removeHandler();
                }
              }
            });
          }
        }
      }
    };  }

  @Override
  public void fireEvent(PushEvent event) {
    eventDispatcher.fire(event);
  }

  private void subscribeForEvent(final PushEvent.Type type, final HandlerRegistration[] handlerRegistration, final PushEventHandler handler) {

    if (eventsMap.containsKey(type.getKey())) {

      handlerRegistration[0] = eventDispatcher.addHandler(type, handler);
      eventsMap.put(type.getKey(), eventsMap.get(type.getKey()) + 1);

    } else {

      pushChannelApi.subscribe(type, new AsyncSubscribeCallback() {

        @Override
        public void onSuccess() {
          handlerRegistration[0] = eventDispatcher.addHandler(type, handler);
          eventsMap.put(type.getKey(), 1);
        }
      });
    }
  }
}
