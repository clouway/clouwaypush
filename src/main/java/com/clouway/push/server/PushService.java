package com.clouway.push.server;

import com.clouway.push.shared.PushEvent;

/**
 * @author Ivan Lazov <ivan.lazov@clouway.com>
 */
public interface PushService {

  void pushEvent(PushEvent event);
}
