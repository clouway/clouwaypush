package com.clouway.push;

import com.clouway.push.core.ChannelFailureException;
import com.clouway.push.core.EventSerializer;
import com.clouway.push.core.HttpClient;
import com.clouway.push.core.IdGenerator;
import com.clouway.push.core.Provider;
import com.clouway.push.core.PushEventSource;
import com.clouway.push.core.UnableToPushEventException;
import com.clouway.push.server.PushEvent;
import com.clouway.push.server.PushService;

/**
 * @author Ivan Lazov <ivan.lazov@clouway.com>
 */
class PushServiceImpl implements PushService {
  private EventSerializer eventSerializer;
  private final HttpClient httpClient;
  private final Provider<String> namespaceProvider;
  private final IdGenerator idGenerator;

  PushServiceImpl(EventSerializer eventSerializer, HttpClient httpClient, Provider<String> namespaceProvider, IdGenerator idGenerator) {
    this.eventSerializer = eventSerializer;
    this.httpClient = httpClient;
    this.namespaceProvider = namespaceProvider;
    this.idGenerator = idGenerator;
  }

  @Override
  public void pushEvent(PushEvent event) {
    pushEvent(event, "");
  }

  @Override
  public void pushEvent(PushEvent event, String correlationId) {
    String id = idGenerator.generate();
    String message = eventSerializer.serialize(new PushEventSource(event, correlationId, id));

    try {
      httpClient.sendMessage(namespaceProvider.get(), event.getKey(), message);
    } catch (ChannelFailureException exception) {
      throw new UnableToPushEventException(exception.getMessage());
    }
  }
}