package com.clouway.push.client;

import com.clouway.push.client.channelapi.AsyncUnsubscribeCallBack;
import com.clouway.push.client.channelapi.Channel;
import com.clouway.push.client.channelapi.ChannelListener;
import com.clouway.push.client.channelapi.PushChannelServiceAsync;
import com.clouway.push.shared.PushEvent;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.client.rpc.SerializationStreamFactory;
import com.google.gwt.user.client.rpc.SerializationStreamReader;
import com.google.inject.Inject;
import com.google.inject.Provider;

/**
 * @author Ivan Lazov <ivan.lazov@clouway.com>
 */
public class PushChannelApiImpl implements PushChannelApi, OnTimeCallBack {

  private final PushChannelServiceAsync pushChannelServiceAsync;
  private final Channel channel;
  private ImAliveTimer timer;
  private Provider<String> subscriber;

  private boolean openedChannel = false;
  private PushEventListener listener;

  @Inject
  public PushChannelApiImpl(PushChannelServiceAsync pushChannelServiceAsync,
                            Channel channel,
                            ImAliveTimer timer,
                            @CurrentSubscriber Provider<String> subscriber) {
    this.pushChannelServiceAsync = pushChannelServiceAsync;
    this.channel = channel;
    this.timer = timer;
    this.subscriber = subscriber;
    timer.onTime(this);
  }

  @Override
  public boolean hasOpenedChannel() {
    return openedChannel;
  }

  @Override
  public void connect() {
    pushChannelServiceAsync.removeSubscriptions(subscriber.get(), new AsyncCallback<Void>(){
      @Override
      public void onFailure(Throwable caught) {

      }

      @Override
      public void onSuccess(Void result) {
        openChannel();
      }
    });
  }

  public void openChannel() {

    pushChannelServiceAsync.openChannel(subscriber.get(), new AsyncCallback<String>() {

      @Override
      public void onFailure(Throwable throwable) {
        openChannel();
      }

      @Override
      public void onSuccess(String channelToken) {

        channel.open(channelToken, new ChannelListener() {

          @Override
          public void onMessage(String json) {
            try {
              SerializationStreamReader reader = ((SerializationStreamFactory) pushChannelServiceAsync).createStreamReader(json);
              PushEvent pushEvent = (PushEvent) reader.readObject();
              listener.onPushEvent(pushEvent);
            } catch (SerializationException e) {
              throw new RuntimeException("Unable to deserialize " + json, e);
            }
          }

          @Override
          public void onTokenExpire() {
            openChannel();
          }
        });

        openedChannel = true;
      }
    });
  }

  @Override
  public void subscribe(final PushEvent.Type type, final AsyncSubscribeCallback callback) {

    pushChannelServiceAsync.subscribe(subscriber.get(), type, new AsyncCallback<Void>() {

      @Override
      public void onFailure(Throwable caught) {
        GWT.log("Subscription of user: " + subscriber.get() + " for event: " + type.getEventName() + " has failed.");
      }

      @Override
      public void onSuccess(Void result) {
        GWT.log("Successfully subscribed user: " + subscriber.get() + " for event: " + type.getEventName());
        callback.onSuccess();
      }
    });
  }

  @Override
  public void addPushEventListener(PushEventListener listener) {
    this.listener = listener;
  }

  @Override
  public void unsubscribe(final PushEvent.Type type, final AsyncUnsubscribeCallBack callBack) {

    pushChannelServiceAsync.unsubscribe(subscriber.get(), type, new AsyncCallback<Void>() {

      @Override
      public void onFailure(Throwable caught) {
      }

      @Override
      public void onSuccess(Void result) {
        callBack.onSuccess();
      }
    });
  }

  @Override
  public void onTime() {

    pushChannelServiceAsync.iAmAlive(subscriber.get(), timer.getSeconds() + 1, new AsyncCallback<Void>() {

      @Override
      public void onFailure(Throwable caught) {
      }

      @Override
      public void onSuccess(Void result) {
      }
    });
  }
}
