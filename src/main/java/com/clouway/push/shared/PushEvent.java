package com.clouway.push.shared;

import com.google.web.bindery.event.shared.Event;

import java.io.Serializable;

/**
 * @author Ivan Lazov <ivan.lazov@clouway.com>
 */
public abstract class PushEvent<T extends PushEventHandler> extends Event<T> implements Serializable {

  protected PushEvent() {
  }

  @Override
  public abstract Type<T> getAssociatedType();

  @Override
  public abstract void dispatch(T handler);

  public static class Type<T> extends Event.Type<T> implements Serializable {
    public Type() {
    }
  }
}
