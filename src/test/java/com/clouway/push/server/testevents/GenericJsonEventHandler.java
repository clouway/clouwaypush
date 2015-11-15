package com.clouway.push.server.testevents;

import com.clouway.push.shared.PushEventHandler;

/**
 * @author Miroslav Genov (miroslav.genov@clouway.com)
 */
public interface GenericJsonEventHandler extends PushEventHandler {

  void onGenericJsonEvent(GenericJsonEvent event);

}
