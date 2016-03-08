package com.clouway.push.server;

import com.google.inject.ImplementedBy;

import java.util.List;

/**
 * @author Mihail Lesikov (mlesikov@gmail.com)
 */
@ImplementedBy(PushChannelServiceImpl.class)
public interface PushChannelService {

  String connect(String subscriber);

  void subscribe(String subscriber, List<String> keys);

  void unsubscribe(String subscriber, String key);

  void keepAlive(String subscriber);
}
