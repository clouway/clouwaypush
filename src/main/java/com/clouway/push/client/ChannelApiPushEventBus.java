package com.clouway.push.client;

import com.clouway.push.shared.PushEvent;
import com.clouway.push.shared.PushEventHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

/**
 * @author Ivan Lazov <ivan.lazov@clouway.com>
 */
public class ChannelApiPushEventBus implements PushEventBus {

  private EventBus eventBus;
  private PushChannelApi pushChannelApi;

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
  public void addHandler(final PushEvent.SerializableType type, final PushEventHandler handler) {

    if (!pushChannelApi.hasOpennedChannel()) {

        pushChannelApi.connect();
    }

    pushChannelApi.subscribe(type, new AsyncSubscribeCallback() {

      @Override
      public void onSuccess() {
        eventBus.addHandler(type,handler);
      }
    });


  }
}
