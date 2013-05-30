package com.clouway.push.shared;

/**
 * @author Ivan Lazov <ivan.lazov@clouway.com>
 */
public class DefaultPushEvent implements PushEvent {

  public DefaultPushEvent() {
  }

  @Override
  public String getEventName() {
    return "DefaultPushEvent";
  }
}
