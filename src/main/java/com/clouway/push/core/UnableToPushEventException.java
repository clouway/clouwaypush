package com.clouway.push.core;

/**
 * @author Ivan Lazov <ivan.lazov@clouway.com>
 */
public class UnableToPushEventException extends RuntimeException {

  private final String message;

  public UnableToPushEventException(String message) {
    this.message = message;
  }

  public String getMessage() {
    return message;
  }
}
