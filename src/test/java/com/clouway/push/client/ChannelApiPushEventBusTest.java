package com.clouway.push.client;

import com.clouway.push.client.channelapi.AsyncUnsubscribeCallBack;
import com.clouway.push.shared.PushEvent;
import com.clouway.push.shared.PushEventHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.SimpleEventBus;
import com.google.web.bindery.event.shared.HandlerRegistration;
import org.jmock.Expectations;
import org.jmock.auto.Mock;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Ivan Lazov <ivan.lazov@clouway.com>
 */
public class ChannelApiPushEventBusTest {

  @Rule
  public final JUnitRuleMockery context = new JUnitRuleMockery();

  private PushEventBus pushEventBus;

  @Mock
  private PushChannelApi pushChannelApi;

  private EventBus eventBus;

  private SimpleEvent event = new SimpleEvent();
  private SimpleEventHandler eventHandler = new SimpleEventHandler();

  private SimpleEvent anotherEvent = new SimpleEvent();
  private SimpleEventHandler anotherEventHandler = new SimpleEventHandler();

  private InstanceCapture<AsyncSubscribeCallback> subscribeCallback = new InstanceCapture<AsyncSubscribeCallback>();
  private InstanceCapture<AsyncUnsubscribeCallBack> unsubscribeCallback = new InstanceCapture<AsyncUnsubscribeCallBack>();

  @Before
  public void setUp() {

    context.checking(new Expectations() {{
      oneOf(pushChannelApi).addPushEventListener(with(any(PushEventListener.class)));
    }});

    eventBus = new SimpleEventBus();

    pushEventBus = new ChannelApiPushEventBus(eventBus, pushChannelApi);
  }

  @Test
  public void addHandlerForEventWhenChannelIsNotOpen() {

    context.checking(new Expectations() {{
      oneOf(pushChannelApi).hasInitialConnection();
      will(returnValue(false));

      oneOf(pushChannelApi).connect();

      oneOf(pushChannelApi).subscribe(with(event.TYPE), with(subscribeCallback));
    }});

    pushEventBus.addHandler(event.TYPE, eventHandler);
  }

  @Test
  public void addHandlerForEventWhenChannelIsOpened() {

    context.checking(new Expectations() {{
      oneOf(pushChannelApi).hasInitialConnection();
      will(returnValue(true));

      never(pushChannelApi).connect();

      oneOf(pushChannelApi).subscribe(with(event.TYPE), with(subscribeCallback));
    }});

    pushEventBus.addHandler(event.TYPE, eventHandler);
  }

  @Test
  public void removeAddedHandlerForEvent() {

    context.checking(new Expectations() {{
      oneOf(pushChannelApi).hasInitialConnection();
      will(returnValue(true));

      oneOf(pushChannelApi).subscribe(with(event.TYPE), with(subscribeCallback));
    }});

    HandlerRegistration handlerRegistration = pushEventBus.addHandler(event.TYPE, eventHandler);
    subscribeCallback.getValue().onSuccess();

    context.checking(new Expectations() {{
      oneOf(pushChannelApi).unsubscribe(with(event.TYPE), with(unsubscribeCallback));
    }});

    handlerRegistration.removeHandler();
  }

  @Test
  public void dispatchFiredEventToSingleHandler() {

    context.checking(new Expectations() {{
      oneOf(pushChannelApi).hasInitialConnection();
      will(returnValue(true));

      oneOf(pushChannelApi).subscribe(with(event.TYPE), with(subscribeCallback));
    }});

    pushEventBus.addHandler(event.TYPE, eventHandler);
    subscribeCallback.getValue().onSuccess();

    eventBus.fireEvent(event);

    assertTrue(eventHandler.dispatched);
  }

  @Test
  public void eventIsNotDispatchedToRemovedHandler() {

    context.checking(new Expectations() {{
      oneOf(pushChannelApi).hasInitialConnection();
      will(returnValue(true));

      oneOf(pushChannelApi).subscribe(with(event.TYPE), with(subscribeCallback));

      oneOf(pushChannelApi).unsubscribe(with(event.TYPE), with(unsubscribeCallback));
    }});

    HandlerRegistration handlerRegistration = pushEventBus.addHandler(event.TYPE, eventHandler);
    subscribeCallback.getValue().onSuccess();

    handlerRegistration.removeHandler();
    unsubscribeCallback.getValue().onSuccess();

    eventBus.fireEvent(event);

    assertFalse(eventHandler.dispatched);
  }

  @Test
  public void addTwoHandlersForSameEvent() {

    context.checking(new Expectations() {{
      oneOf(pushChannelApi).hasInitialConnection();
      will(returnValue(true));

      oneOf(pushChannelApi).subscribe(with(event.TYPE), with(subscribeCallback));
    }});

    pushEventBus.addHandler(event.TYPE, eventHandler);
    subscribeCallback.getValue().onSuccess();

    context.checking(new Expectations() {{
      oneOf(pushChannelApi).hasInitialConnection();
      will(returnValue(true));

      never(pushChannelApi).subscribe(with(event.TYPE), with(any(AsyncSubscribeCallback.class)));
    }});

    pushEventBus.addHandler(event.TYPE, anotherEventHandler);
  }

  @Test
  public void dispatchFiredEventToTwoHandlers() {

    context.checking(new Expectations() {{
      oneOf(pushChannelApi).hasInitialConnection();
      will(returnValue(true));

      oneOf(pushChannelApi).subscribe(with(event.TYPE), with(subscribeCallback));
    }});

    pushEventBus.addHandler(event.TYPE, eventHandler);
    subscribeCallback.getValue().onSuccess();

    context.checking(new Expectations() {{
      oneOf(pushChannelApi).hasInitialConnection();
      will(returnValue(true));

      never(pushChannelApi).subscribe(with(event.TYPE), with(any(AsyncSubscribeCallback.class)));
    }});

    pushEventBus.addHandler(event.TYPE, anotherEventHandler);

    eventBus.fireEvent(event);

    assertTrue(eventHandler.dispatched);
    assertTrue(anotherEventHandler.dispatched);
  }

  @Test
  public void addTwoHandlersForEventRemoveOnlyOneOfThem() {

    context.checking(new Expectations() {{
      oneOf(pushChannelApi).hasInitialConnection();
      will(returnValue(true));

      oneOf(pushChannelApi).subscribe(with(event.TYPE), with(subscribeCallback));
    }});

    HandlerRegistration handlerRegistration = pushEventBus.addHandler(event.TYPE, eventHandler);
    subscribeCallback.getValue().onSuccess();

    context.checking(new Expectations() {{
      oneOf(pushChannelApi).hasInitialConnection();
      will(returnValue(true));

      never(pushChannelApi).subscribe(with(event.TYPE), with(any(AsyncSubscribeCallback.class)));
    }});

    pushEventBus.addHandler(event.TYPE, anotherEventHandler);

    context.checking(new Expectations() {{
      never(pushChannelApi).unsubscribe(with(event.TYPE), with(any(AsyncUnsubscribeCallBack.class)));
    }});

    handlerRegistration.removeHandler();
  }

  @Test
  public void addTwoHandlersForEventRemoveAllOfThem() {

    context.checking(new Expectations() {{
      oneOf(pushChannelApi).hasInitialConnection();
      will(returnValue(true));

      oneOf(pushChannelApi).subscribe(with(event.TYPE), with(subscribeCallback));
    }});

    HandlerRegistration handlerRegistration = pushEventBus.addHandler(event.TYPE, eventHandler);
    subscribeCallback.getValue().onSuccess();

    context.checking(new Expectations() {{
      oneOf(pushChannelApi).hasInitialConnection();
      will(returnValue(true));

      never(pushChannelApi).subscribe(with(event.TYPE), with(any(AsyncSubscribeCallback.class)));
    }});

    HandlerRegistration anotherHandlerRegistration = pushEventBus.addHandler(event.TYPE, anotherEventHandler);

    context.checking(new Expectations() {{
      never(pushChannelApi).unsubscribe(with(event.TYPE), with(any(AsyncUnsubscribeCallBack.class)));
      oneOf(pushChannelApi).unsubscribe(with(event.TYPE), with(any(AsyncUnsubscribeCallBack.class)));
    }});

    handlerRegistration.removeHandler();
    anotherHandlerRegistration.removeHandler();
  }

  @Test
  public void addHandlerAfterRemovingAllHandlersForEvent() {

    context.checking(new Expectations() {{
      oneOf(pushChannelApi).hasInitialConnection();
      will(returnValue(true));

      oneOf(pushChannelApi).subscribe(with(event.TYPE), with(subscribeCallback));
    }});

    HandlerRegistration handlerRegistration = pushEventBus.addHandler(event.TYPE, eventHandler);
    subscribeCallback.getValue().onSuccess();

    context.checking(new Expectations() {{
      oneOf(pushChannelApi).unsubscribe(with(event.TYPE), with(unsubscribeCallback));
    }});

    handlerRegistration.removeHandler();
    unsubscribeCallback.getValue().onSuccess();

    context.checking(new Expectations() {{
      oneOf(pushChannelApi).hasInitialConnection();
      will(returnValue(true));

      oneOf(pushChannelApi).subscribe(with(event.TYPE), with(any(AsyncSubscribeCallback.class)));
    }});

    pushEventBus.addHandler(event.TYPE, eventHandler);
  }

  @Test
  public void shouldNotOpenMoreThanOneChanelPerSubscriber() throws Exception {

    context.checking(new Expectations() {{

      //add first event
      oneOf(pushChannelApi).hasInitialConnection();
      will(returnValue(false));
      oneOf(pushChannelApi).connect();
      oneOf(pushChannelApi).subscribe(with(event.TYPE), with(any(AsyncSubscribeCallback.class)));

      //add second event
      oneOf(pushChannelApi).hasInitialConnection();
      will(returnValue(true));
      oneOf(pushChannelApi).subscribe(with(anotherEvent.TYPE), with(any(AsyncSubscribeCallback.class)));

    }});

    pushEventBus.addHandler(event.TYPE, eventHandler);
    pushEventBus.addHandler(anotherEvent.TYPE, anotherEventHandler);
  }

  public class SimpleEventHandler implements PushEventHandler {

    boolean dispatched = false;

    public void onEvent() {
      dispatched = true;
    }
  }

  public class SimpleEvent extends PushEvent<SimpleEventHandler> {

    public Type<SimpleEventHandler> TYPE = new Type<SimpleEventHandler>("SimpleEvent");

    public Type<SimpleEventHandler> getAssociatedType() {
      return TYPE;
    }

    public void dispatch(SimpleEventHandler handler) {
      handler.onEvent();
    }
  }
}

