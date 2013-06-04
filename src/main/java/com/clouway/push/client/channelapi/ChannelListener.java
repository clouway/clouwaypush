package com.clouway.push.client.channelapi;

/**
 * @author Ivan Lazov <ivan.lazov@clouway.com>
 */
public interface ChannelListener {

  void onMessage(String json);

  void onTokenExpire();
}
