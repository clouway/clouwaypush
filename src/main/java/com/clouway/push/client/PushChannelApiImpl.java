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

  private boolean openedChannel = false;
  private int subscribingAttempts;
  private int unsubscribingAttempts;
  private int timesSubscribed;

  private PushEventListener listener;

  @Inject
  public PushChannelApiImpl(PushChannelServiceAsync pushChannelServiceAsync,
                            Channel channel,
                            KeepAliveTimer timer,
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
  public void connect(final AsyncConnectCallback connectCallback) {
    establishNewConnection(connectCallback);
  }

  private void establishNewConnection(final AsyncConnectCallback connectCallback) {

    pushChannelServiceAsync.connect(subscriber.get(), new AsyncCallback<String>() {

      @Override
      public void onFailure(Throwable caught) {
      }

      @Override
      public void onSuccess(String channelToken) {

        openChannel(channelToken);

        openedChannel = true;
        connectCallback.onConnect();
      }
    });
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

    pushChannelServiceAsync.subscribe(subscriber.get(), type, new AsyncCallback<Void>() {

      public void onFailure(Throwable caught) {

        List<Integer> secondsDelays = timer.getSecondsDelays();

        if (subscribingAttempts < secondsDelays.size()) {

          timer.scheduleTimedAction(subscribingAttempts, secondsDelays, new TimedAction() {

            public void execute() {
              subscribingAttempts++;
              subscribe(type, callback);
            }
          });

        } else {
          subscribingAttempts = 0;
          throw new UnableToSubscribeForEventException();
        }
      }

      public void onSuccess(Void result) {
        subscribingAttempts = 0;
        timesSubscribed++;
        callback.onSuccess();
      }
    });
  }

  @Override
  public void unsubscribe(final PushEvent.Type type, final AsyncUnsubscribeCallBack callback) {

    pushChannelServiceAsync.unsubscribe(subscriber.get(), type, new AsyncCallback<Void>() {

      public void onFailure(Throwable caught) {

        List<Integer> secondsDelays = timer.getSecondsDelays();

        if (unsubscribingAttempts < secondsDelays.size()) {
          timer.scheduleTimedAction(unsubscribingAttempts, secondsDelays, new TimedAction() {
            @Override
            public void execute() {
              unsubscribingAttempts++;
              unsubscribe(type, callback);
            }
          });
        } else {
          unsubscribingAttempts = 0;
          throw new UnableToUnsubscribeFromEventException();
        }
      }

      public void onSuccess(Void result) {
        unsubscribingAttempts = 0;
        timesSubscribed--;
        callback.onSuccess();
      }
    });
  }

  @Override
  public void onTime() {

    if (timesSubscribed > 0) {

      pushChannelServiceAsync.keepAlive(subscriber.get(), timer.getSeconds(), new AsyncCallback<Void>() {

        public void onFailure(Throwable caught) {
        }

        public void onSuccess(Void result) {
        }
      });
    }
  }

  @Override
  public void addPushEventListener(PushEventListener listener) {
    this.listener = listener;
  }
}
