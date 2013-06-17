package com.clouway.push.shared;

/**
 * @author Ivan Lazov <ivan.lazov@clouway.com>
 */
public class DefaultEvent extends PushEvent<DefaultEventHandler> {

  public static Type<DefaultEventHandler> TYPE = new Type<DefaultEventHandler>("DefaultEvent");

  @Override
  public Type<DefaultEventHandler> getAssociatedType() {
    return TYPE;
  }

  @Override
  public void dispatch(DefaultEventHandler handler) {
  }
}
