package com.clouway.push.server;

import com.clouway.push.shared.PushEvent;

/**
 * Used to serialize given {@link PushEvent} to string.
 *
 * @author Georgi Georgiev (GeorgievJon@gmail.com)
 */
public interface Encoder {

  String encode(PushEvent event);
}
