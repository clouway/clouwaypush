package com.clouway.push.server;

/**
 * @author Mihail Lesikov (mlesikov@gmail.com)
 */
public class PushEventSource {

  public final PushEvent event;

  public final String  correlationId;

  public PushEventSource(PushEvent event, String correlationId) {
    this.event = event;
    this.correlationId = correlationId;
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
