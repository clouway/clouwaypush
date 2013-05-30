package com.clouway.push.client;

import com.clouway.push.shared.PushEvent;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.jmock.Expectations;
import org.jmock.auto.Mock;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

/**
 * @author Ivan Lazov <ivan.lazov@clouway.com>
 */
public class PushChannelImplTest {

  @Rule
  public final JUnitRuleMockery context = new JUnitRuleMockery();

  @Mock
  private PushChannelServiceAsync pushChannelServiceAsync;

  @Mock
  private DefaultServiceAsync defaultServiceAsync;

  @Mock
  private PushEventBus pushEventBus;

  private PushChannel pushChannel;

  private final DummyEvent event = new DummyEvent();
  private final DummyEventHandler eventHandler = new DummyEventHandler();

  @Before
  public void setUp() {
    pushChannel = new PushChannelImpl(pushChannelServiceAsync, defaultServiceAsync, pushEventBus);
  }

  @Test
  public void openNewChannel() {

    final AsyncAction<String> asyncAction = new AsyncAction<String>();

    context.checking(new Expectations() {{
      oneOf(pushChannelServiceAsync).open(with(any(AsyncCallback.class)));
      will(asyncAction);
    }});

    pushChannel.open();
  }

  @Test
  public void retryOpeningChannelOnFailure() {

    final AsyncAction<String> asyncAction = new AsyncAction<String>();

    context.checking(new Expectations() {{
      oneOf(pushChannelServiceAsync).open(with(any(AsyncCallback.class)));
      will(asyncAction);
    }});

    pushChannel.open();

    context.checking(new Expectations() {{
      oneOf(pushChannelServiceAsync).open(with(any(AsyncCallback.class)));
      will(new AsyncAction<String>());
    }});

    asyncAction.onFailure(new RuntimeException());
  }

  @Test
  public void subscribeForEvent() {

    final AsyncAction<Void> asyncAction = new AsyncAction<Void>();

    context.checking(new Expectations() {{
      oneOf(pushChannelServiceAsync).subscribe(with(event), with(any(AsyncCallback.class)));
      will(asyncAction);

      oneOf(pushEventBus).addHandler(event, eventHandler);
    }});

    pushChannel.subscribe(event, eventHandler);
    asyncAction.onSuccess(null);
  }

  @Test
  public void retrySubscribingForEventOnFailure() {

    final AsyncAction<Void> asyncAction = new AsyncAction<Void>();

    context.checking(new Expectations() {{
      oneOf(pushChannelServiceAsync).subscribe(with(event), with(any(AsyncCallback.class)));
      will(asyncAction);
    }});

    pushChannel.subscribe(event, eventHandler);

    context.checking(new Expectations() {{
      oneOf(pushChannelServiceAsync).subscribe(with(event), with(any(AsyncCallback.class)));
      will(new AsyncAction<Void>());
    }});

    asyncAction.onFailure(new RuntimeException());
  }

  @Test
  public void unsubscribeFromEvent() {

    final AsyncAction<Void> asyncAction = new AsyncAction<Void>();

    context.checking(new Expectations() {{
      oneOf(pushChannelServiceAsync).unsubscribe(with(event), with(any(AsyncCallback.class)));
      will(asyncAction);

      oneOf(pushEventBus).removeHandlers(event);
    }});

    pushChannel.unsubscribe(event);
    asyncAction.onSuccess(null);
  }

  @Test
  public void retryUnsubscribingFromEvent() {

    final AsyncAction<Void> asyncAction = new AsyncAction<Void>();

    context.checking(new Expectations() {{
      oneOf(pushChannelServiceAsync).unsubscribe(with(event), with(any(AsyncCallback.class)));
      will(asyncAction);
    }});

    pushChannel.unsubscribe(event);

    context.checking(new Expectations() {{
      oneOf(pushChannelServiceAsync).unsubscribe(with(event), with(any(AsyncCallback.class)));
    }});

    asyncAction.onFailure(new RuntimeException());
  }

  private class DummyEvent implements PushEvent {

    @Override
    public String getEventName() {
      return "DummyEvent";
    }
  }

  private class DummyEventHandler implements PushEventHandler<DummyEvent> {

    @Override
    public void onEvent(DummyEvent event) {
    }
  }
}
