package com.clouway.push.client;

import com.clouway.push.client.channelapi.AsyncUnsubscribeCallBack;
import com.clouway.push.shared.PushEvent;
import com.clouway.push.shared.PushEventHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.HandlerRegistration;

/**
 * @author Ivan Lazov <ivan.lazov@clouway.com>
 */
public class ChannelApiPushEventBus implements PushEventBus {

  private final EventBus eventBus;
  private final PushChannelApi pushChannelApi;

  @Inject
  public ChannelApiPushEventBus(final EventBus eventBus, PushChannelApi pushChannelApi) {

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

    if (!pushChannelApi.hasOpenedChannel()) {

      pushChannelApi.connect(new AsyncConnectCallback() {

        @Override
        public void onConnect() {

          pushChannelApi.subscribe(type, new AsyncSubscribeCallback() {

            @Override
            public void onSuccess() {
              handlerRegistration[0] = eventBus.addHandler(type, handler);
            }
          });
        }
      });
    } else {

      pushChannelApi.subscribe(type, new AsyncSubscribeCallback() {
        @Override
        public void onSuccess() {
          handlerRegistration[0] = eventBus.addHandler(type, handler);
        }
      });
    }

    return new HandlerRegistration() {
      @Override
      public void removeHandler() {

        pushChannelApi.unsubscribe(type, new AsyncUnsubscribeCallBack() {
          @Override
          public void onSuccess() {
            if (handlerRegistration[0] != null) {
              handlerRegistration[0].removeHandler();
            }
          }
        });
      }
    };
  }
}
