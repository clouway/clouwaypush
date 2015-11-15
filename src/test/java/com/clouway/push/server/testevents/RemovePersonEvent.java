package com.clouway.push.server.testevents;

import com.clouway.push.shared.PushEvent;

/**
 * @author Miroslav Genov (miroslav.genov@clouway.com)
 */
public class RemovePersonEvent extends PushEvent<RemovePersonEventHandler> {

  private static final Type<RemovePersonEventHandler> TYPE = new Type<RemovePersonEventHandler>("removePersonEvent");

  private final Integer personId;

  public RemovePersonEvent(Integer personId) {
    this.personId = personId;
  }

  @Override
  public Type<RemovePersonEventHandler> getAssociatedType() {
    return TYPE;
  }

  @Override
  public void dispatch(RemovePersonEventHandler handler) {
    handler.onPersonRemoved(this);
  }
}
