package com.clouway.push.client;

import com.clouway.push.shared.PushEvent;
import org.jmock.Expectations;
import org.jmock.auto.Mock;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

/**
 * @author Ivan Lazov <ivan.lazov@clouway.com>
 */
public class SimplePushEventBusTest {

  @Rule
  public final JUnitRuleMockery context = new JUnitRuleMockery();

  private PushEventBus pushEventBus;

  @Mock
  private PushEventHandler<DummyEvent> eventHandler;

  @Mock
  private PushEventHandler<DummyEvent> anotherEventHandler;

  private final DummyEvent event = new DummyEvent();

  @Before
  public void setUp() {
    pushEventBus = new SimplePushEventBus();
  }

  @Test
  public void fireEventWithSingleHandler() {

    context.checking(new Expectations() {{
      oneOf(eventHandler).onEvent(event);
    }});

    pushEventBus.addHandler(event, eventHandler);
    pushEventBus.fireEvent(event);
  }

  @Test
  public void fireEventWithMultipleHandlers() {

    context.checking(new Expectations() {{
      oneOf(eventHandler).onEvent(event);
      oneOf(anotherEventHandler).onEvent(event);
    }});

    pushEventBus.addHandler(event, eventHandler);
    pushEventBus.addHandler(event, anotherEventHandler);

    pushEventBus.fireEvent(event);
  }

  @Test(expected = PushEventHandlerNotFoundException.class)
  public void fireEventWithoutHandler() {

    pushEventBus.fireEvent(new DummyEvent());
  }

  @Test(expected = PushEventHandlerNotFoundException.class)
  public void removeSingleEventHandler() {

    pushEventBus.addHandler(event, eventHandler);
    pushEventBus.removeHandlers(event);

    context.checking(new Expectations() {{
      never(eventHandler);
    }});

    pushEventBus.fireEvent(event);
  }

  @Test(expected = PushEventHandlerNotFoundException.class)
  public void removeMultipleEventHandlers() {

    pushEventBus.addHandler(event, eventHandler);
    pushEventBus.addHandler(event, anotherEventHandler);

    pushEventBus.removeHandlers(event);

    context.checking(new Expectations() {{
      never(eventHandler);
      never(anotherEventHandler);
    }});

    pushEventBus.fireEvent(event);
  }

  @Test
  public void removeSpecificEventHandler() {

    pushEventBus.addHandler(event, eventHandler);
    pushEventBus.removeHandler(event, eventHandler);

    context.checking(new Expectations() {{
      never(eventHandler);
    }});

    pushEventBus.fireEvent(event);
  }

  private class DummyEvent implements PushEvent {

    @Override
    public String getEventName() {
      return "DummyEvent";
    }
  }
}
