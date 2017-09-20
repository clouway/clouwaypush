package com.clouway.push.server;

/**
 * @author Ivan Lazov <ivan.lazov@clouway.com>
 */
public class PushEvent {

  private final String key;

  protected PushEvent(String key) {
    this.key = key;
  }

  public String getKey() {
    return key;
  }
}
