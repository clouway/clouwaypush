package com.clouway.push.server.testevents;

import com.clouway.push.shared.PushEvent;

/**
 * @author Miroslav Genov (miroslav.genov@clouway.com)
 */
public final class AddPersonEvent extends PushEvent<AddPersonEventHandler> {
  private static final Type<AddPersonEventHandler> TYPE = new Type<AddPersonEventHandler>("addPersonEvent");

  public String name;
  public Integer age;

  public AddPersonEvent(String name, Integer age) {
    this.name = name;
    this.age = age;
  }

  @Override
  public Type<AddPersonEventHandler> getAssociatedType() {
    return TYPE;
  }

  @Override
  public void dispatch(AddPersonEventHandler handler) {
    handler.onPersonAdded(this);
  }
}
