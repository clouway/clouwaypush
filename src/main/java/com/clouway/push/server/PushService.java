package com.clouway.push.server;

/**
 * @author Ivan Lazov <ivan.lazov@clouway.com>
 */
public interface PushService {

  void pushEvent(PushEvent event);

  void pushEvent(PushEvent event, String correlationId);
}
