package com.clouway.push.client;

import com.clouway.push.client.channelapi.AsyncUnsubscribeCallBack;
import com.clouway.push.client.channelapi.Channel;
import com.clouway.push.client.channelapi.ChannelListener;
import com.clouway.push.client.channelapi.PushChannelServiceAsync;
import com.clouway.push.shared.PushEvent;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.client.rpc.SerializationStreamFactory;
import com.google.gwt.user.client.rpc.SerializationStreamReader;
import com.google.inject.Inject;
import com.google.inject.Provider;

import java.util.List;

/**
 * @author Ivan Lazov <ivan.lazov@clouway.com>
 */
public class PushChannelApiImpl implements PushChannelApi {

  private final PushChannelServiceAsync pushChannelServiceAsync;
  private final Channel channel;
  private final KeepAliveTimer timer;
  private final Provider<String> subscriber;
  private Provider<List<Integer>> subscribeRequestSecondsRetries;
  private Provider<List<Integer>> unsubscribeRequestSecondsRetries;
  private Provider<List<Integer>> keepAliveRequestSecondsRetries;

  //Marker which indicate that already is made request for open of connection.
  //It can be used for prevent sending second async request for open of connection while
  // waiting response of first async request.
  private boolean initialConnection = false;

  private int subscriptionsCount;
  private PushEventListener listener;

  @Inject
  public PushChannelApiImpl(PushChannelServiceAsync pushChannelServiceAsync,
                            Channel channel,
                            KeepAliveTimer timer,
                            @CurrentSubscriber Provider<String> subscriber,
                            @SubscribeRequestSecondsRetries Provider<List<Integer>> subscribeRequestSecondsRetries,
                            @UnsubscribeRequestSecondsRetries Provider<List<Integer>> unsubscribeRequestSecondsRetries,
                            @KeepAliveRequestSecondsRetries Provider<List<Integer>> keepAliveRequestSecondsRetries) {

    this.pushChannelServiceAsync = pushChannelServiceAsync;
    this.channel = channel;
    this.timer = timer;
    this.subscriber = subscriber;
    this.subscribeRequestSecondsRetries = subscribeRequestSecondsRetries;
    this.unsubscribeRequestSecondsRetries = unsubscribeRequestSecondsRetries;
    this.keepAliveRequestSecondsRetries = keepAliveRequestSecondsRetries;

    timer.onTime(this);
  }

  @Override
  public boolean hasInitialConnection() {
    return initialConnection;
  }

  @Override
  public void connect() {
    if(!initialConnection) {
      initialConnection = true;
      establishNewConnection();
    }
  }

  private void establishNewConnection() {

    pushChannelServiceAsync.connect(subscriber.get(), new AsyncCallback<String>() {

      public void onFailure(Throwable caught) {
      }

      public void onSuccess(String channelToken) {
        openChannel(channelToken);
      }
    });
  }

  private void openChannel(String channelToken) {

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
        establishNewConnection();
      }
    });
  }

  @Override
  public void subscribe(final PushEvent.Type type, final AsyncSubscribeCallback callback) {
    subscribeForEvent(0, subscribeRequestSecondsRetries.get(), type, callback);
  }

  private void subscribeForEvent(final int position, final List<Integer> secondsRetries, final PushEvent.Type type, final AsyncSubscribeCallback callback) {

    pushChannelServiceAsync.subscribe(subscriber.get(), type, new AsyncCallback<Void>() {

      @Override
      public void onFailure(Throwable caught) {

        if (position < secondsRetries.size()) {

          timer.scheduleAction(secondsRetries.get(position), new TimerAction() {

            @Override
            public void execute() {
              int nextPosition = position + 1;
              subscribeForEvent(nextPosition, secondsRetries, type, callback);
            }
          });

        } else {
          throw new UnableToSubscribeForEventException();

        }
      }

      @Override
      public void onSuccess(Void result) {
        increaseSubscriptionsCount();
        callback.onSuccess();
      }
    });
  }

  @Override
  public void unsubscribe(final PushEvent.Type type, final AsyncUnsubscribeCallBack callback) {
    unsubscribeFromEvent(0, unsubscribeRequestSecondsRetries.get(), type, callback);
  }

  private void unsubscribeFromEvent(final int position, final List<Integer> secondsRetries, final PushEvent.Type type, final AsyncUnsubscribeCallBack callback) {

    pushChannelServiceAsync.unsubscribe(subscriber.get(), type, new AsyncCallback<Void>() {

      @Override
      public void onFailure(Throwable caught) {

        if (position < secondsRetries.size()) {

          timer.scheduleAction(secondsRetries.get(position), new TimerAction() {

            @Override
            public void execute() {
              int nextPosition = position + 1;
              unsubscribeFromEvent(nextPosition, secondsRetries, type, callback);
            }
          });
        } else {
          throw new UnableToUnsubscribeFromEventException();
        }
      }

      @Override
      public void onSuccess(Void result) {
        decreaseSubscriptionsCount();
        callback.onSuccess();
      }
    });
  }

  @Override
  public void onTime() {
    onTime(0, keepAliveRequestSecondsRetries.get());
  }

  private void onTime(final int position, final List<Integer> secondsRetries) {

    if (hasSubscriptions()) {

      pushChannelServiceAsync.keepAlive(subscriber.get(), new AsyncCallback<Void>() {

        @Override
        public void onFailure(Throwable caught) {

          if (position < secondsRetries.size()) {

            timer.scheduleAction(secondsRetries.get(position), new TimerAction() {

              @Override
              public void execute() {
                int nextPosition = position + 1;
                onTime(nextPosition, secondsRetries);
              }
            });
          } else {
            throw new SubscriberNotAliveException();
          }
        }

        @Override
        public void onSuccess(Void result) {
        }
      });
    }
  }

  private boolean hasSubscriptions() {
    return subscriptionsCount > 0;
  }

  private void decreaseSubscriptionsCount() {
    if (subscriptionsCount > 0) {
      subscriptionsCount--;
    }
  }

  private void increaseSubscriptionsCount() {
    subscriptionsCount++;
  }

  @Override
  public void addPushEventListener(PushEventListener listener) {
    this.listener = listener;
  }
}
