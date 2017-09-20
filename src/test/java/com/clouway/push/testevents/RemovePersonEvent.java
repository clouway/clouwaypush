package com.clouway.push.testevents;

import com.clouway.push.server.PushEvent;

/**
 * @author Miroslav Genov (miroslav.genov@clouway.com)
 */
public class RemovePersonEvent extends PushEvent {

  private final Integer personId;

  public RemovePersonEvent(Integer personId) {
    super("removePersonEvent");
    this.personId = personId;
  }

}
