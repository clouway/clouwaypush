package com.clouway.push.client.channelapi;

/**
 * @author Ivan Lazov <ivan.lazov@clouway.com>
 */
public interface Channel {

  void open(String channelToken, ChannelListener listener);
}
