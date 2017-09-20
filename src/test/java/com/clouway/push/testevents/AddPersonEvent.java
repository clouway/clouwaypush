package com.clouway.push.testevents;

import com.clouway.push.server.PushEvent;

/**
 * @author Miroslav Genov (miroslav.genov@clouway.com)
 */
public final class AddPersonEvent extends PushEvent {

  public String name;
  public Integer age;

  public AddPersonEvent(String name, Integer age) {
    super("addPersonEvent");
    this.name = name;
    this.age = age;
  }
}
