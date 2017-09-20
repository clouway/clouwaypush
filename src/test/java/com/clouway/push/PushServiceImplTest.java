package com.clouway.push;

import com.clouway.push.core.ChannelFailureException;
import com.clouway.push.core.EventSerializer;
import com.clouway.push.core.HttpClient;
import com.clouway.push.core.IdGenerator;
import com.clouway.push.core.Provider;
import com.clouway.push.core.PushEventSource;
import com.clouway.push.core.UnableToPushEventException;
import com.clouway.push.server.PushService;
import org.jmock.Expectations;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class PushServiceImplTest {

  @Rule
  public final JUnitRuleMockery context = new JUnitRuleMockery();

  private EventSerializer eventSerializer = context.mock(EventSerializer.class);
  private HttpClient channelService = context.mock(HttpClient.class);
  private IdGenerator idGenerator = context.mock(IdGenerator.class);
  private Provider<String> namespaceProvider = context.mock(Provider.class);


  private PushService pushService;

  @Before
  public void setUp() throws Exception {
    pushService = new PushServiceImpl(eventSerializer, channelService, namespaceProvider, idGenerator);
  }

  @Test
  public void pushEventToClient() throws Exception {
    final DefaultEvent event = new DefaultEvent();
    final String eventMessage = "event message";

    context.checking(new Expectations() {{
      oneOf(idGenerator).generate();
      will(returnValue("id"));

      oneOf(namespaceProvider).get();
      will(returnValue("namespace"));

      oneOf(eventSerializer).serialize(new PushEventSource(event, "", "id"));
      will(returnValue(eventMessage));

      oneOf(channelService).sendMessage(with("namespace"), with("DefaultEvent"), with(eventMessage));
    }});

    pushService.pushEvent(event);
  }

  @Test(expected = UnableToPushEventException.class)
  public void pushEventIsFailed() throws Exception {

    final DefaultEvent event = new DefaultEvent();
    final String eventMessage = "event message";

    context.checking(new Expectations() {{
      oneOf(idGenerator).generate();
      will(returnValue("id"));

      oneOf(namespaceProvider).get();
      will(returnValue("namespace"));

      oneOf(eventSerializer).serialize(new PushEventSource(event, "", "id"));
      will(returnValue(eventMessage));

      oneOf(channelService).sendMessage(with("namespace"), with("DefaultEvent"), with(eventMessage));
      will(throwException(new ChannelFailureException("message")));
    }});

    pushService.pushEvent(event);

  }

  @Test
  public void pushEventToClientWithCorrelationId() throws Exception {
    final DefaultEvent event = new DefaultEvent();

    context.checking(new Expectations() {{
      oneOf(idGenerator).generate();
      will(returnValue("id"));

      oneOf(namespaceProvider).get();
      will(returnValue("namespace"));

      oneOf(eventSerializer).serialize(new PushEventSource(event, "correlationId", "id"));
      will(returnValue("{\"event\":{\"key\":\"DefaultEvent\"},\"correlationId\":\"correlationId\",\"id\":\"id\"}"));

      oneOf(channelService).sendMessage(with("namespace"), with("DefaultEvent"), with("{\"event\":{\"key\":\"DefaultEvent\"},\"correlationId\":\"correlationId\",\"id\":\"id\"}"));
    }});

    pushService.pushEvent(event, "correlationId");
  }

  @Test(expected = UnableToPushEventException.class)
  public void pushingEventWithCorrelationIdFails() throws Exception {

    final DefaultEvent event = new DefaultEvent();
    final String eventMessage = "event message";

    context.checking(new Expectations() {{
      oneOf(idGenerator).generate();
      will(returnValue("id"));

      oneOf(namespaceProvider).get();
      will(returnValue("namespace"));

      oneOf(eventSerializer).serialize(new PushEventSource(event, "correlationId", "id"));
      will(returnValue(eventMessage));

      oneOf(channelService).sendMessage(with("namespace"), with("DefaultEvent"), with(eventMessage));
      will(throwException(new ChannelFailureException("message")));
    }});

    pushService.pushEvent(event, "correlationId");

  }
}