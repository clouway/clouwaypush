package com.clouway.push.client.channelapi;

/**
 * @author Ivan Lazov <ivan.lazov@clouway.com>
 */
public class ChannelImpl implements Channel {

  @Override
  public void open(String channelToken, ChannelListener listener) {

    openChannel(channelToken, listener);
  }

  private native void openChannel(String channelToken, ChannelListener listener) /*-{

      var channel = new $wnd.goog.appengine.Channel(channelToken);
      var socket = channel.open();

      socket.onmessage = function (event) {
          listener.@com.clouway.push.client.channelapi.ChannelListener::onMessage(Ljava/lang/String;)(event.data);
      }

      socket.onerror = function (event) {
          listener.@com.clouway.push.client.channelapi.ChannelListener::onTokenExpire()();
      }
  }-*/;
}
