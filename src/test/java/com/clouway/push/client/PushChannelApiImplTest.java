package com.clouway.push.client;

import com.clouway.push.client.channelapi.AsyncUnsubscribeCallBack;
import com.clouway.push.client.channelapi.Channel;
import com.clouway.push.client.channelapi.ChannelListener;
import com.clouway.push.client.channelapi.PushChannelServiceAsync;
import com.clouway.push.shared.PushEvent;
import com.clouway.push.shared.PushEventHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.util.Providers;
import org.jmock.Expectations;
import org.jmock.auto.Mock;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * @author Ivan Lazov <ivan.lazov@clouway.com>
 */
public class PushChannelApiImplTest {

  @Rule
  public final JUnitRuleMockery context = new JUnitRuleMockery();

  private PushChannelApi pushChannel;

  @Mock
  private PushChannelServiceAsync pushChannelServiceAsync;

  @Mock
  private Channel channel;

  @Mock
  private KeepAliveTimer timer;

  private String subscriber = "john@gmail.com";

  private SimpleEvent event = new SimpleEvent();

  private AsyncSubscribeCallbackImpl subscribeCallback = new AsyncSubscribeCallbackImpl();

  private AsyncUnsubscribeCallbackImpl unsubscribeCallback = new AsyncUnsubscribeCallbackImpl();

  private AsyncAction<Void> asyncAction = new AsyncAction<Void>();

  private InstanceCapture<TimerAction> timerAction = new InstanceCapture<TimerAction>();

  final InstanceCapture<ChannelListener> channelListener = new InstanceCapture<ChannelListener>();

  final List<Integer> subscribeRequestSecondsRetries = new ArrayList<Integer>();
  final List<Integer> unsubscribeRequestSecondsRetries = new ArrayList<Integer>();
  final List<Integer> keepAliveRequestSecondsRetries = new ArrayList<Integer>();

  @Before
  public void setUp() {

    subscribeRequestSecondsRetries.add(1);
    unsubscribeRequestSecondsRetries.add(1);
    keepAliveRequestSecondsRetries.add(3);

    context.checking(new Expectations() {{
      oneOf(timer).onTime(with(any(OnTimeCallBack.class)));
    }});

    pushChannel = new PushChannelApiImpl(pushChannelServiceAsync,
                                         channel,
                                         timer,
                                         Providers.of(subscriber),
                                         Providers.of(subscribeRequestSecondsRetries),
                                         Providers.of(unsubscribeRequestSecondsRetries),
                                         Providers.of(keepAliveRequestSecondsRetries));
  }

  @Test
  public void subscribeForEvent() {

    context.checking(new Expectations() {{
      oneOf(pushChannelServiceAsync).subscribe(with(subscriber), with(event.TYPE), with(any(AsyncCallback.class)));
      will(asyncAction);
    }});

    pushChannel.subscribe(event.TYPE, subscribeCallback);
    asyncAction.onSuccess(null);

    assertThat(subscribeCallback.timesCalled, is(1));
  }

  @Test
  public void retrySubscribingForEventOnce() {

    context.checking(new Expectations() {{
      oneOf(pushChannelServiceAsync).subscribe(with(subscriber), with(event.TYPE), with(any(AsyncCallback.class)));
      will(asyncAction);

      oneOf(timer).scheduleAction(with(any(Integer.class)), with(timerAction));
    }});

    pushChannel.subscribe(event.TYPE, subscribeCallback);
    asyncAction.onFailure(new RuntimeException());

    context.checking(new Expectations() {{
      oneOf(pushChannelServiceAsync).subscribe(with(subscriber), with(event.TYPE), with(any(AsyncCallback.class)));
      will(asyncAction);

      never(timer);
    }});

    timerAction.getValue().execute();
    asyncAction.onSuccess(null);

    assertThat(subscribeCallback.timesCalled, is(1));
  }

  @Test(expected = UnableToSubscribeForEventException.class)
  public void unableToSubscribeForEvent() {

    context.checking(new Expectations() {{
      oneOf(pushChannelServiceAsync).subscribe(with(subscriber), with(event.TYPE), with(any(AsyncCallback.class)));
      will(asyncAction);

      oneOf(timer).scheduleAction(with(any(Integer.class)), with(timerAction));
    }});

    pushChannel.subscribe(event.TYPE, subscribeCallback);
    asyncAction.onFailure(new RuntimeException());

    context.checking(new Expectations() {{
      oneOf(pushChannelServiceAsync).subscribe(with(subscriber), with(event.TYPE), with(any(AsyncCallback.class)));
      will(asyncAction);
    }});

    timerAction.getValue().execute();
    asyncAction.onFailure(new RuntimeException());

    assertThat(subscribeCallback.timesCalled, is(0));
  }

  @Test
  public void unsubscribeFromEvent() {

    context.checking(new Expectations() {{
      oneOf(pushChannelServiceAsync).unsubscribe(with(subscriber), with(event.TYPE), with(any(AsyncCallback.class)));
      will(asyncAction);
    }});

    pushChannel.unsubscribe(event.TYPE, unsubscribeCallback);
    asyncAction.onSuccess(null);

    assertThat(unsubscribeCallback.timesCalled, is(1));
  }

  @Test
  public void retryUnsubscribingFromEventOnce() {

    context.checking(new Expectations() {{
      oneOf(pushChannelServiceAsync).unsubscribe(with(subscriber), with(event.TYPE), with(any(AsyncCallback.class)));
      will(asyncAction);

      oneOf(timer).scheduleAction(with(any(Integer.class)), with(timerAction));
    }});

    pushChannel.unsubscribe(event.TYPE, unsubscribeCallback);
    asyncAction.onFailure(new RuntimeException());

    context.checking(new Expectations() {{
      oneOf(pushChannelServiceAsync).unsubscribe(with(subscriber), with(event.TYPE), with(any(AsyncCallback.class)));
      will(asyncAction);

      never(timer);
    }});

    timerAction.getValue().execute();
    asyncAction.onSuccess(null);

    assertThat(unsubscribeCallback.timesCalled, is(1));
  }

  @Test(expected = UnableToUnsubscribeFromEventException.class)
  public void unableToUnsubscribeFromEvent() {

    context.checking(new Expectations() {{
      oneOf(pushChannelServiceAsync).unsubscribe(with(subscriber), with(event.TYPE), with(any(AsyncCallback.class)));
      will(asyncAction);

      oneOf(timer).scheduleAction(with(any(Integer.class)), with(timerAction));
    }});

    pushChannel.unsubscribe(event.TYPE, unsubscribeCallback);
    asyncAction.onFailure(new RuntimeException());

    context.checking(new Expectations() {{
      oneOf(pushChannelServiceAsync).unsubscribe(with(subscriber), with(event.TYPE), with(any(AsyncCallback.class)));
      will(asyncAction);

      never(timer);
    }});

    timerAction.getValue().execute();
    asyncAction.onFailure(null);

    assertThat(unsubscribeCallback.timesCalled, is(0));
  }

  @Test
  public void keepAliveSubscriberWhoHasSubscriptions() {

    context.checking(new Expectations() {{
      oneOf(pushChannelServiceAsync).subscribe(with(subscriber), with(event.TYPE), with(any(AsyncCallback.class)));
      will(asyncAction);
    }});

    pushChannel.subscribe(event.TYPE, subscribeCallback);
    asyncAction.onSuccess(null);

    context.checking(new Expectations() {{
      oneOf(pushChannelServiceAsync).keepAlive(with(subscriber), with(any(AsyncCallback.class)));
    }});

    pushChannel.onTime();
  }

  @Test
  public void retrySendingRequestThatSubscriberIsAlive() {

    context.checking(new Expectations() {{
      oneOf(pushChannelServiceAsync).subscribe(with(subscriber), with(event.TYPE), with(any(AsyncCallback.class)));
      will(asyncAction);
    }});

    pushChannel.subscribe(event.TYPE, subscribeCallback);
    asyncAction.onSuccess(null);

    context.checking(new Expectations() {{
      oneOf(pushChannelServiceAsync).keepAlive(with(subscriber), with(any(AsyncCallback.class)));
      will(asyncAction);
    }});

    pushChannel.onTime();

    context.checking(new Expectations() {{
      oneOf(timer).scheduleAction(with(any(Integer.class)), with(timerAction));
    }});

    asyncAction.onFailure(new RuntimeException());

    context.checking(new Expectations() {{
      oneOf(pushChannelServiceAsync).keepAlive(with(subscriber), with(any(AsyncCallback.class)));
      will(asyncAction);
    }});

    timerAction.getValue().execute();
  }

  @Test(expected = SubscriberNotAliveException.class)
  public void unableToKeepSubscriberAlive() {

    context.checking(new Expectations() {{
      oneOf(pushChannelServiceAsync).subscribe(with(subscriber), with(event.TYPE), with(any(AsyncCallback.class)));
      will(asyncAction);
    }});

    pushChannel.subscribe(event.TYPE, subscribeCallback);
    asyncAction.onSuccess(null);

    context.checking(new Expectations() {{
      oneOf(pushChannelServiceAsync).keepAlive(with(subscriber), with(any(AsyncCallback.class)));
      will(asyncAction);
    }});

    pushChannel.onTime();

    context.checking(new Expectations() {{
      oneOf(timer).scheduleAction(with(any(Integer.class)), with(timerAction));
    }});

    asyncAction.onFailure(new RuntimeException());

    context.checking(new Expectations() {{
      oneOf(pushChannelServiceAsync).keepAlive(with(subscriber), with(any(AsyncCallback.class)));
      will(asyncAction);
    }});

    timerAction.getValue().execute();
    asyncAction.onFailure(new RuntimeException());
  }

  @Test
  public void subscriberWithoutSubscriptionsIsNotKeptAlive() {

    context.checking(new Expectations() {{
      oneOf(pushChannelServiceAsync).subscribe(with(subscriber), with(event.TYPE), with(any(AsyncCallback.class)));
      will(asyncAction);
    }});

    pushChannel.subscribe(event.TYPE, subscribeCallback);
    asyncAction.onSuccess(null);

    context.checking(new Expectations() {{
      oneOf(pushChannelServiceAsync).unsubscribe(with(subscriber), with(event.TYPE), with(any(AsyncCallback.class)));
      will(asyncAction);
    }});

    pushChannel.unsubscribe(event.TYPE, unsubscribeCallback);
    asyncAction.onSuccess(null);

    context.checking(new Expectations() {{
      never(timer);
      never(pushChannelServiceAsync);
    }});

    pushChannel.onTime();
  }

  @Test
  public void openConnection() throws Exception {

    context.checking(new Expectations() {{
      oneOf(pushChannelServiceAsync).connect(with(subscriber), with(any(AsyncCallback.class)));
      will(asyncAction);
    }});

    pushChannel.connect();
    assertTrue(pushChannel.hasInitialConnection());
  }

  @Test
  public void openNewChannel() {

    final AsyncAction<String> asyncAction = new AsyncAction<String>();

    context.checking(new Expectations() {{
      oneOf(pushChannelServiceAsync).connect(with(subscriber), with(any(AsyncCallback.class)));
      will(asyncAction);

      oneOf(channel).open(with("channelToken"), with(channelListener));
    }});

    pushChannel.connect();
    asyncAction.onSuccess("channelToken");

    assertTrue(pushChannel.hasInitialConnection());
  }

  @Test
  public void reopenChannelWhenTokenExpires() {

    final AsyncAction<String> asyncAction = new AsyncAction<String>();

    context.checking(new Expectations() {{
      oneOf(pushChannelServiceAsync).connect(with(subscriber), with(any(AsyncCallback.class)));
      will(asyncAction);

      oneOf(channel).open(with("channelToken"), with(channelListener));
    }});

    pushChannel.connect();
    asyncAction.onSuccess("channelToken");

    context.checking(new Expectations() {{
      oneOf(pushChannelServiceAsync).connect(with(subscriber), with(any(AsyncCallback.class)));
      will(asyncAction);

      oneOf(channel).open(with("newChannelToken"), with(channelListener));
    }});

    channelListener.getValue().onTokenExpire();
    asyncAction.onSuccess("newChannelToken");

    assertTrue(pushChannel.hasInitialConnection());
  }

  private class AsyncUnsubscribeCallbackImpl implements AsyncUnsubscribeCallBack {

    int timesCalled = 0;

    public void onSuccess() {
      timesCalled++;
    }
  }

  private class AsyncSubscribeCallbackImpl implements AsyncSubscribeCallback {

    int timesCalled = 0;

    public void onSuccess() {
      timesCalled++;
    }
  }

  private interface SimpleEventHandler extends PushEventHandler {
  }

  private class SimpleEvent extends PushEvent<SimpleEventHandler> {

    public Type<SimpleEventHandler> TYPE = new Type<SimpleEventHandler>("SimplEvent");

    public Type<SimpleEventHandler> getAssociatedType() {
      return TYPE;
    }

    public void dispatch(SimpleEventHandler handler) {
    }
  }
}
