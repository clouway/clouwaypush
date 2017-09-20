package com.clouway.push;

import com.clouway.push.server.PushEvent;

/**
 * @author Ivan Lazov <ivan.lazov@clouway.com>
 */
public class DefaultEvent extends PushEvent {

  public DefaultEvent() {
    super("DefaultEvent");
  }
}
