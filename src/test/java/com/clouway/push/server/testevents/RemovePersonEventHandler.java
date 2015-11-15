package com.clouway.push.server.testevents;

import com.clouway.push.shared.PushEventHandler;

/**
 * @author Miroslav Genov (miroslav.genov@clouway.com)
 */
public interface RemovePersonEventHandler extends PushEventHandler {

  void onPersonRemoved(RemovePersonEvent event);

}
