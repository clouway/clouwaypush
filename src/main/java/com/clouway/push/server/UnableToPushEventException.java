package com.clouway.push.server;

import com.clouway.push.shared.PushEvent;

/**
 * @author Ivan Lazov <ivan.lazov@clouway.com>
 */
public class UnableToPushEventException extends RuntimeException {

  private final PushEvent event;
  private final String message;

  public UnableToPushEventException(PushEvent event, String message) {
    this.event = event;
    this.message = message;
  }

  public PushEvent getEvent() {
    return event;
  }

  public String getMessage() {
    return message;
  }
}
