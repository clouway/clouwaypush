package com.clouway.push.server.testevents;

import com.clouway.push.server.PushEvent;

/**
 * @author Miroslav Genov (miroslav.genov@clouway.com)
 */
public class GenericJsonEvent extends PushEvent {

  public GenericJsonEvent() {
    super("genericJsonEvent");
  }

}
