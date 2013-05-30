package com.clouway.push.client;

import com.clouway.push.shared.PushEvent;

/**
 * @author Ivan Lazov <ivan.lazov@clouway.com>
 */
public interface PushChannel {

  void open();

  void subscribe(PushEvent event, PushEventHandler eventHandler);
}
