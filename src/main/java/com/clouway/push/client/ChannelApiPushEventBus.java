package com.clouway.push.client;

import com.clouway.push.client.channelapi.AsyncUnsubscribeCallBack;
import com.clouway.push.shared.PushEvent;
import com.clouway.push.shared.PushEventHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.google.web.bindery.event.shared.HandlerRegistration;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Ivan Lazov <ivan.lazov@clouway.com>
 */
public class ChannelApiPushEventBus implements PushEventBus {

  private final EventBus eventBus;
  private final PushChannelApi pushChannelApi;
  private final Map<String, Integer> eventsMap = new HashMap<String, Integer>();

  @Inject
  public ChannelApiPushEventBus(@Named("PushEventBus") final EventBus eventBus, PushChannelApi pushChannelApi) {

    this.eventBus = eventBus;
    this.pushChannelApi = pushChannelApi;

    pushChannelApi.addPushEventListener(new PushEventListener() {

      @Override
      public void onPushEvent(PushEvent event) {
        eventBus.fireEvent(event);
      }
    });
  }

  @Override
  public HandlerRegistration addHandler(final PushEvent.Type type, final PushEventHandler handler) {

    final HandlerRegistration[] handlerRegistration = {null};

    if (!pushChannelApi.hasInitialConnection()) {

      pushChannelApi.connect(new AsyncConnectCallback() {

        public void onConnect() {
        }
      });

    }

    //subscription for request is independent from opening of channel
    subscribeForEvent(type, handlerRegistration, handler);


    return new HandlerRegistration() {

      public void removeHandler() {

        if (eventsMap.containsKey(type.getEventName())) {

          eventsMap.put(type.getEventName(), eventsMap.get(type.getEventName()) - 1);

          if (eventsMap.get(type.getEventName()) == 0) {

            eventsMap.remove(type.getEventName());

            pushChannelApi.unsubscribe(type, new AsyncUnsubscribeCallBack() {

              public void onSuccess() {

                if (handlerRegistration[0] != null) {
                  handlerRegistration[0].removeHandler();
                }
              }
            });
          }
        }
      }
    };
  }

  private void subscribeForEvent(final PushEvent.Type type, final HandlerRegistration[] handlerRegistration, final PushEventHandler handler) {

    if (eventsMap.containsKey(type.getEventName())) {

      handlerRegistration[0] = eventBus.addHandler(type, handler);
      eventsMap.put(type.getEventName(), eventsMap.get(type.getEventName()) + 1);

    } else {

      pushChannelApi.subscribe(type, new AsyncSubscribeCallback() {

        @Override
        public void onSuccess() {
          handlerRegistration[0] = eventBus.addHandler(type, handler);
          eventsMap.put(type.getEventName(), 1);
        }
      });
    }
  }
}
