package com.clouway.push.core;

import com.clouway.push.server.PushEvent;

/**
 * @author Mihail Lesikov (mlesikov@gmail.com)
 */
public class PushEventSource {

  public final PushEvent event;

  public final String  correlationId;

  public final String id;

  public PushEventSource(PushEvent event, String correlationId, String id) {
    this.event = event;
    this.correlationId = correlationId;
    this.id = id;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof PushEventSource)) return false;

    PushEventSource that = (PushEventSource) o;

    if (event != null ? !event.equals(that.event) : that.event != null) return false;
    return correlationId != null ? correlationId.equals(that.correlationId) : that.correlationId == null;

  }

  @Override
  public int hashCode() {
    int result = event != null ? event.hashCode() : 0;
    result = 31 * result + (correlationId != null ? correlationId.hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return "PushEventSource{" +
            "event=" + event +
            ", correlationId='" + correlationId + '\'' +
            '}';
  }
}
