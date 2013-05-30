package com.clouway.push.client;

import com.clouway.push.shared.PushEvent;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.client.rpc.SerializationStreamFactory;
import com.google.gwt.user.client.rpc.SerializationStreamReader;
import com.google.inject.Inject;

/**
 * @author Ivan Lazov <ivan.lazov@clouway.com>
 */
public class PushChannelImpl implements PushChannel {

  private final PushChannelServiceAsync pushChannelServiceAsync;
  private final DefaultServiceAsync defaultServiceAsync;
  private final PushEventBus pushEventBus;

  @Inject
  public PushChannelImpl(PushChannelServiceAsync pushChannelServiceAsync, DefaultServiceAsync defaultServiceAsync, PushEventBus pushEventBus) {
    this.pushChannelServiceAsync = pushChannelServiceAsync;
    this.defaultServiceAsync = defaultServiceAsync;
    this.pushEventBus = pushEventBus;
  }

  @Override
  public void open() {

    pushChannelServiceAsync.open(new AsyncCallback<String>() {

      @Override
      public void onFailure(Throwable throwable) {
        open();
      }

      @Override
      public void onSuccess(String channelToken) {
        openChannel(channelToken, PushChannelImpl.this);
      }
    });
  }

  @Override
  public void subscribe(final PushEvent event, final PushEventHandler eventHandler) {

    pushChannelServiceAsync.subscribe(event, new AsyncCallback<Void>() {

      @Override
      public void onFailure(Throwable throwable) {
        subscribe(event, eventHandler);
      }

      @Override
      public void onSuccess(Void aVoid) {
        pushEventBus.addHandler(event, eventHandler);
      }
    });
  }

  @Override
  public void unsubscribe(final PushEvent event) {

    pushChannelServiceAsync.unsubscribe(event, new AsyncCallback<Void>() {
      @Override
      public void onFailure(Throwable caught) {
        unsubscribe(event);
      }

      @Override
      public void onSuccess(Void result) {
        pushEventBus.removeHandlers(event);
      }
    });
  }

  private native void openChannel(String channelToken, PushChannelImpl gwtPushChannel) /*-{

      var channel = new $wnd.goog.appengine.Channel(channelToken);
      var socket = channel.open();

      socket.onmessage = function (event) {
          gwtPushChannel.@com.clouway.push.client.PushChannelImpl::onMessage(Ljava/lang/String;)(event.data);
      }

  }-*/;

  private void onMessage(String json) {

    try {
      SerializationStreamReader reader = ((SerializationStreamFactory) defaultServiceAsync).createStreamReader(json);
      PushEvent pushEvent = (PushEvent) reader.readObject();
      pushEventBus.fireEvent(pushEvent);
    } catch (SerializationException e) {
      throw new RuntimeException("Unable to deserialize " + json, e);
    }
  }
}
