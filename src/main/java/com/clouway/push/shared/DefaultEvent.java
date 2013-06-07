package com.clouway.push.shared;

/**
 * @author Ivan Lazov <ivan.lazov@clouway.com>
 */
public class DefaultEvent extends PushEvent<DefaultHandler> {

  public static Type<DefaultHandler> TYPE = new Type<DefaultHandler>() {
    @Override
    public String getEventName() {
      return "DefaultEvent";
    }
  };

  @Override
  public Type<DefaultHandler> getAssociatedType() {
    return TYPE;
  }

  @Override
  public void dispatch(DefaultHandler handler) {
  }
}
