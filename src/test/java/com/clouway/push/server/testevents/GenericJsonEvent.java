package com.clouway.push.server.testevents;

import com.clouway.push.shared.PushEvent;

/**
 * @author Miroslav Genov (miroslav.genov@clouway.com)
 */
public class GenericJsonEvent extends PushEvent<GenericJsonEventHandler> {
  private static final Type<GenericJsonEventHandler> TYPE = new Type<GenericJsonEventHandler>("genericJsonEvent");

  @Override
  public Type<GenericJsonEventHandler> getAssociatedType() {
    return TYPE;
  }

  @Override
  public void dispatch(GenericJsonEventHandler handler) {
    handler.onGenericJsonEvent(this);
  }
}
