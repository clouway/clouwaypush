package com.clouway.push.core;

import com.clouway.push.server.PushEvent;

/**
 * Used to serialize given {@link PushEvent} to string.
 *
 * @author Georgi Georgiev (GeorgievJon@gmail.com)
 */
public interface EventSerializer {

  /**
   * Encodes the provided event as String value.
   *
   * @param eventSource the event that needs to be encoded
   * @return the encoded value of event
   */
  String serialize(PushEventSource eventSource);

}
