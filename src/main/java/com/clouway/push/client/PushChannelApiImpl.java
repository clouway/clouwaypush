package com.clouway.push.client;

import com.clouway.push.client.channelapi.PushChannelServiceAsync;
import com.clouway.push.shared.PushEvent;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.client.rpc.SerializationStreamFactory;
import com.google.gwt.user.client.rpc.SerializationStreamReader;
import com.google.inject.Inject;

/**
 * @author Ivan Lazov <ivan.lazov@clouway.com>
 */
public class PushChannelApiImpl implements PushChannelApi {

  private PushChannelServiceAsync pushChannelServiceAsync;

  private boolean openedChannel = false;
  private PushEventListener listener;

  @Inject
  public PushChannelApiImpl(PushChannelServiceAsync pushChannelServiceAsync) {
    this.pushChannelServiceAsync = pushChannelServiceAsync;
  }

  @Override
  public boolean hasOpennedChannel() {
    return openedChannel;
  }

  @Override
  public void connect() {

    pushChannelServiceAsync.open(new AsyncCallback<String>() {

      @Override
      public void onFailure(Throwable throwable) {
        connect();
      }

      @Override
      public void onSuccess(String channelToken) {
        openChannel(channelToken, PushChannelApiImpl.this);
        openedChannel = true;
      }
    });
  }

  private native void openChannel(String channelToken, PushChannelApiImpl pushChannelApi) /*-{

      var channel = new $wnd.goog.appengine.Channel(channelToken);
      var socket = channel.open();

      socket.onmessage = function (event) {
          pushChannelApi.@com.clouway.push.client.PushChannelApiImpl::onMessage(Ljava/lang/String;)(event.data);
      }

  }-*/;

  private void onMessage(String json) {

    try {
      SerializationStreamReader reader = ((SerializationStreamFactory) pushChannelServiceAsync).createStreamReader(json);
      PushEvent pushEvent = (PushEvent) reader.readObject();
      listener.onPushEvent(pushEvent);
    } catch (SerializationException e) {
      throw new RuntimeException("Unable to deserialize " + json, e);
    }
  }

  @Override
  public void subscribe(PushEvent.Type type, final AsyncSubscribeCallback callback) {

    pushChannelServiceAsync.subscribe(type, new AsyncCallback<Void>() {
      @Override
      public void onFailure(Throwable caught) {
      }

      @Override
      public void onSuccess(Void result) {
        callback.onSuccess();
      }
    });
  }

  @Override
  public void addPushEventListener(PushEventListener listener) {

    this.listener = listener;
  }
}
