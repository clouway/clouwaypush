package com.clouway.push.client;

import com.clouway.push.client.channelapi.Channel;
import com.clouway.push.client.channelapi.ChannelListener;
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

  private final PushChannelServiceAsync pushChannelServiceAsync;
  private final Channel channel;

  private boolean openedChannel = false;
  private PushEventListener listener;

  @Inject
  public PushChannelApiImpl(PushChannelServiceAsync pushChannelServiceAsync, Channel channel) {
    this.pushChannelServiceAsync = pushChannelServiceAsync;
    this.channel = channel;
  }

  @Override
  public boolean hasOpenedChannel() {
    return openedChannel;
  }

  @Override
  public void openChannel() {

    pushChannelServiceAsync.openChannel(new AsyncCallback<String>() {

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
