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

  private InstanceCapture<TimedAction> timedAction = new InstanceCapture<TimedAction>();

  private InstanceCapture<Integer> retries = new InstanceCapture<Integer>();

  private List<Integer> secondsDelays = new ArrayList<Integer>();

  final AsyncConnectCallbackImpl connectCallback = new AsyncConnectCallbackImpl();

  final InstanceCapture<ChannelListener> channelListener = new InstanceCapture<ChannelListener>();

  @Before
  public void setUp() {

    secondsDelays.add(1);

    context.checking(new Expectations() {{
      oneOf(timer).onTime(with(any(OnTimeCallBack.class)));
    }});

    pushChannel = new PushChannelApiImpl(pushChannelServiceAsync, channel, timer, Providers.of(subscriber));
  }

  @Test
  public void subscribeForEvent() {

    context.checking(new Expectations() {{
      oneOf(pushChannelServiceAsync).subscribe(with(subscriber), with(event.TYPE), with(any(AsyncCallback.class)));
      will(asyncAction);
    }});

    pushChannel.subscribe(event.TYPE, subscribeCallback);
    asyncAction.onSuccess(null);

    assertTrue(subscribeCallback.subscribed);
  }

  @Test
  public void retrySubscribingForEventSeveralTimes() {

    secondsDelays.add(2);

    context.checking(new Expectations() {{
      oneOf(pushChannelServiceAsync).subscribe(with(subscriber), with(event.TYPE), with(any(AsyncCallback.class)));
      will(asyncAction);
    }});

    pushChannel.subscribe(event.TYPE, subscribeCallback);


    context.checking(new Expectations() {{
      oneOf(timer).getSecondsDelays();
      will(returnValue(secondsDelays));
      oneOf(timer).scheduleTimedAction(with(retries), with(secondsDelays), with(timedAction));
    }});

    asyncAction.onFailure(new RuntimeException());
    assertThat(retries.getValue(), is(0));


    context.checking(new Expectations() {{
      oneOf(pushChannelServiceAsync).subscribe(with(subscriber), with(event.TYPE), with(any(AsyncCallback.class)));
      will(asyncAction);
    }});

    timedAction.getValue().execute();


    context.checking(new Expectations() {{
      oneOf(timer).getSecondsDelays();
      will(returnValue(secondsDelays));

      oneOf(timer).scheduleTimedAction(with(retries), with(secondsDelays), with(timedAction));
    }});

    asyncAction.onFailure(new RuntimeException());
    assertThat(retries.getValue(), is(1));
  }

  @Test(expected = UnableToSubscribeForEventException.class)
  public void unableToSubscribeForEvent() {

    context.checking(new Expectations() {{
      oneOf(pushChannelServiceAsync).subscribe(with(subscriber), with(event.TYPE), with(any(AsyncCallback.class)));
      will(asyncAction);
    }});

    pushChannel.subscribe(event.TYPE, subscribeCallback);


    context.checking(new Expectations() {{
      oneOf(timer).getSecondsDelays();
      will(returnValue(secondsDelays));
      oneOf(timer).scheduleTimedAction(with(retries), with(secondsDelays), with(timedAction));
    }});

    asyncAction.onFailure(new RuntimeException());


    context.checking(new Expectations() {{
      oneOf(pushChannelServiceAsync).subscribe(with(subscriber), with(event.TYPE), with(any(AsyncCallback.class)));
      will(asyncAction);
    }});

    timedAction.getValue().execute();


    context.checking(new Expectations() {{
      oneOf(timer).getSecondsDelays(); will(returnValue(secondsDelays));
    }});

    asyncAction.onFailure(new RuntimeException());

    assertThat(retries.getValue(), is(1));
  }

  @Test
  public void unsubscribeFromEvent() {

    context.checking(new Expectations() {{
      oneOf(pushChannelServiceAsync).unsubscribe(with(subscriber), with(event.TYPE), with(any(AsyncCallback.class)));
      will(asyncAction);
    }});

    pushChannel.unsubscribe(event.TYPE, unsubscribeCallback);
    asyncAction.onSuccess(null);

    assertTrue(unsubscribeCallback.unsubscribed);
  }

  @Test
  public void retryUnsubscribingFromEventSeveralTimes() {

    secondsDelays.add(2);

    context.checking(new Expectations() {{
      oneOf(pushChannelServiceAsync).unsubscribe(with(subscriber), with(event.TYPE), with(any(AsyncCallback.class)));
      will(asyncAction);
    }});

    pushChannel.unsubscribe(event.TYPE, unsubscribeCallback);


    context.checking(new Expectations() {{
      oneOf(timer).getSecondsDelays();
      will(returnValue(secondsDelays));

      oneOf(timer).scheduleTimedAction(with(retries), with(secondsDelays), with(timedAction));
    }});

    asyncAction.onFailure(new RuntimeException());
    assertThat(retries.getValue(), is(0));


    context.checking(new Expectations() {{
      oneOf(pushChannelServiceAsync).unsubscribe(with(subscriber), with(event.TYPE), with(any(AsyncCallback.class)));
      will(asyncAction);
    }});

    timedAction.getValue().execute();


    context.checking(new Expectations() {{
      oneOf(timer).getSecondsDelays();
      will(returnValue(secondsDelays));

      oneOf(timer).scheduleTimedAction(with(retries), with(secondsDelays), with(timedAction));
    }});

    asyncAction.onFailure(new RuntimeException());

    assertThat(retries.getValue(), is(1));
  }

  @Test(expected = UnableToUnsubscribeFromEventException.class)
  public void unableToUnsubscribeFromEvent() {

    context.checking(new Expectations() {{
      oneOf(pushChannelServiceAsync).unsubscribe(with(subscriber), with(event.TYPE), with(any(AsyncCallback.class)));
      will(asyncAction);
    }});

    pushChannel.unsubscribe(event.TYPE, unsubscribeCallback);


    context.checking(new Expectations() {{
      oneOf(timer).getSecondsDelays();
      will(returnValue(secondsDelays));

      oneOf(timer).scheduleTimedAction(with(retries), with(secondsDelays), with(timedAction));
    }});

    asyncAction.onFailure(new RuntimeException());


    context.checking(new Expectations() {{
      oneOf(pushChannelServiceAsync).unsubscribe(with(subscriber), with(event.TYPE), with(any(AsyncCallback.class)));
      will(asyncAction);
    }});

    timedAction.getValue().execute();


    context.checking(new Expectations() {{
      oneOf(timer).getSecondsDelays();
      will(returnValue(secondsDelays));
    }});

    asyncAction.onFailure(new RuntimeException());
  }

  @Test
  public void keepAliveSubscribedSubscribers() {

    context.checking(new Expectations() {{
      oneOf(pushChannelServiceAsync).subscribe(with(subscriber), with(event.TYPE), with(any(AsyncCallback.class)));
      will(asyncAction);
    }});

    pushChannel.subscribe(event.TYPE, subscribeCallback);
    asyncAction.onSuccess(null);

    context.checking(new Expectations() {{
      oneOf(timer).getSeconds();
      will(returnValue(60));

      oneOf(pushChannelServiceAsync).keepAlive(with(subscriber), with(60), with(any(AsyncCallback.class)));
    }});

    pushChannel.onTime();
  }

  @Test
  public void unsubscribedSubscribersAreNotKeptAlive() {

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

    context.checking(new Expectations(){{
      never(timer);
      never(pushChannelServiceAsync);
    }});

    pushChannel.onTime();
  }

  @Test
  public void openNewChannel() {

    final AsyncAction<String> asyncAction = new AsyncAction<String>();

    context.checking(new Expectations() {{
      oneOf(pushChannelServiceAsync).openChannel(with(subscriber), with(any(AsyncCallback.class)));
      will(asyncAction);

      oneOf(channel).open(with("channelToken"), with(channelListener));
    }});

    pushChannel.connect(connectCallback);
    asyncAction.onSuccess("channelToken");

    assertTrue(pushChannel.hasOpenedChannel());
    assertTrue(connectCallback.connected);
  }

  @Test
  public void openNewChannelWhenChannelTokenExpires() {

    final AsyncAction<String> asyncAction = new AsyncAction<String>();

    context.checking(new Expectations() {{
      oneOf(pushChannelServiceAsync).openChannel(with(subscriber), with(any(AsyncCallback.class)));
      will(asyncAction);

      oneOf(channel).open(with("channelToken"), with(channelListener));
    }});

    pushChannel.connect(connectCallback);
    asyncAction.onSuccess("channelToken");

    context.checking(new Expectations() {{
      oneOf(pushChannelServiceAsync).openChannel(with(subscriber), with(any(AsyncCallback.class)));
      will(asyncAction);
    }});

    channelListener.getValue().onTokenExpire();

    context.checking(new Expectations() {{
      oneOf(channel).open(with("channelToken"), with(channelListener));
    }});
    asyncAction.onSuccess("channelToken");

    assertThat(connectCallback.timesConnected, is(1));
    assertTrue(pushChannel.hasOpenedChannel());
  }

  private class AsyncConnectCallbackImpl implements AsyncConnectCallback {

    boolean connected = false;
    int timesConnected = 0;

    public void onConnect() {
      connected = true;
      timesConnected++;
    }
  }

  private class AsyncUnsubscribeCallbackImpl implements AsyncUnsubscribeCallBack {

    boolean unsubscribed = false;

    public void onSuccess() {
      unsubscribed = true;
    }
  }

  private class AsyncSubscribeCallbackImpl implements AsyncSubscribeCallback {

    boolean subscribed = false;

    public void onSuccess() {
      subscribed = true;
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

//  @Test
//  public void openNewChannel() {

//    final AsyncAction<String> asyncAction = new AsyncAction<String>();
//    final InstanceCapture<ChannelListener> channelListener = new InstanceCapture<ChannelListener>();
//
//    context.checking(new Expectations() {{
//      oneOf(pushChannelServiceAsync).openChannel(with(any(AsyncCallback.class)));
//      will(asyncAction);
//
//      oneOf(channel).open(with("channelToken"), with(channelListener));
//    }});
//
//    pushChannel.openChannel();
//    asyncAction.onSuccess("channelToken");
//
//    assertThat(pushChannel.hasOpenedChannel(), is(true));
//  }

//  @Test
//  public void reopenChannelWhenTokenExpires() {
//
//    final AsyncAction<String> asyncAction = new AsyncAction<String>();
//    final InstanceCapture<ChannelListener> channelListener = new InstanceCapture<ChannelListener>();
//
//    context.checking(new Expectations() {{
//      oneOf(pushChannelServiceAsync).openChannel(with(any(AsyncCallback.class)));
//      will(asyncAction);
//
//      oneOf(channel).open(with("channelToken"), with(channelListener));
//    }});
//
//    pushChannel.openChannel();
//    asyncAction.onSuccess("channelToken");
//
//    final AsyncAction<String> anotherAsyncAction = new AsyncAction<String>();
//    final InstanceCapture<ChannelListener> anotherChannelListener = new InstanceCapture<ChannelListener>();
//
//    context.checking(new Expectations() {{
//      oneOf(pushChannelServiceAsync).openChannel(with(any(AsyncCallback.class)));
//      will(anotherAsyncAction);
//
//      oneOf(channel).open(with("newChannelToken"), with(anotherChannelListener));
//    }});
//
//    channelListener.getValue().onTokenExpire();
//    anotherAsyncAction.onSuccess("newChannelToken");
//  }
//
//  @Test
//  public void retryOpeningNewChannelOnFailure() {
//
//    final AsyncAction<String> asyncAction = new AsyncAction<String>();
//    final AsyncAction<String> retryAction = new AsyncAction<String>();
//    final InstanceCapture<ChannelListener> channelListener = new InstanceCapture<ChannelListener>();
//
//    context.checking(new Expectations() {{
//      oneOf(pushChannelServiceAsync).openChannel(with(any(AsyncCallback.class)));
//      will(asyncAction);
//      never(channel);
//
//      oneOf(pushChannelServiceAsync).openChannel(with(any(AsyncCallback.class)));
//      will(retryAction);
//      oneOf(channel).open(with("channelToken"), with(channelListener));
//    }});
//
//    pushChannel.openChannel();
//    asyncAction.onFailure(new RuntimeException());
//    retryAction.onSuccess("channelToken");
//  }
}
