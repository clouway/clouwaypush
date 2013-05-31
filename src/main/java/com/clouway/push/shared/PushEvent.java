package com.clouway.push.shared;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.google.web.bindery.event.shared.Event;

/**
 * @author Ivan Lazov <ivan.lazov@clouway.com>
 */
public abstract class PushEvent<T extends PushEventHandler> extends Event<T> implements IsSerializable {

  protected PushEvent() {
  }

  @Override
  public abstract SerializableType<T> getAssociatedType();

  @Override
  public abstract void dispatch(T handler);

  public static class SerializableType<T> extends Event.Type<T> implements IsSerializable {
    public SerializableType() {
    }
  }
}
