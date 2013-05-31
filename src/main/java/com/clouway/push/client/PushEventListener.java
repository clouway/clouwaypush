package com.clouway.push.client;

import com.clouway.push.shared.PushEvent;

/**
 * @author Ivan Lazov <ivan.lazov@clouway.com>
 */
public interface PushEventListener {

  void onPushEvent(PushEvent event);
}
