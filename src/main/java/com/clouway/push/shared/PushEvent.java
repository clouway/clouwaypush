package com.clouway.push.shared;

import java.io.Serializable;

/**
 * @author Ivan Lazov <ivan.lazov@clouway.com>
 */
public interface PushEvent extends Serializable {

  String getEventName();
}
