package com.clouway.push.client;

import com.clouway.push.client.channelapi.Channel;
import com.clouway.push.client.channelapi.ChannelListener;
import com.clouway.push.client.channelapi.PushChannelServiceAsync;
import com.clouway.push.shared.PushEvent;
import com.clouway.push.shared.PushEventHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.jmock.Expectations;
import org.jmock.auto.Mock;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

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

  @Before
  public void setUp() {
    pushChannel = new PushChannelApiImpl(pushChannelServiceAsync, channel);
  }

  @Test
  public void openNewChannel() {

    final AsyncAction<String> asyncAction = new AsyncAction<String>();
    final InstanceCapture<ChannelListener> channelListener = new InstanceCapture<ChannelListener>();

    context.checking(new Expectations() {{
      oneOf(pushChannelServiceAsync).openChannel(with(any(AsyncCallback.class)));
      will(asyncAction);

      oneOf(channel).open(with("channelToken"), with(channelListener));
    }});

    pushChannel.openChannel();
    asyncAction.onSuccess("channelToken");

    assertThat(pushChannel.hasOpenedChannel(), is(true));
  }

  @Test
  public void reopenChannelWhenTokenExpires() {

    final AsyncAction<String> asyncAction = new AsyncAction<String>();
    final InstanceCapture<ChannelListener> channelListener = new InstanceCapture<ChannelListener>();

    context.checking(new Expectations() {{
      oneOf(pushChannelServiceAsync).openChannel(with(any(AsyncCallback.class)));
      will(asyncAction);

      oneOf(channel).open(with("channelToken"), with(channelListener));
    }});

    pushChannel.openChannel();
    asyncAction.onSuccess("channelToken");

    final AsyncAction<String> anotherAsyncAction = new AsyncAction<String>();
    final InstanceCapture<ChannelListener> anotherChannelListener = new InstanceCapture<ChannelListener>();

    context.checking(new Expectations() {{
      oneOf(pushChannelServiceAsync).openChannel(with(any(AsyncCallback.class)));
      will(anotherAsyncAction);

      oneOf(channel).open(with("newChannelToken"), with(anotherChannelListener));
    }});

    channelListener.getValue().onTokenExpire();
    anotherAsyncAction.onSuccess("newChannelToken");
  }

  @Test
  public void retryOpeningNewChannelOnFailure() {

    final AsyncAction<String> asyncAction = new AsyncAction<String>();
    final AsyncAction<String> retryAction = new AsyncAction<String>();
    final InstanceCapture<ChannelListener> channelListener = new InstanceCapture<ChannelListener>();

    context.checking(new Expectations() {{
      oneOf(pushChannelServiceAsync).openChannel(with(any(AsyncCallback.class)));
      will(asyncAction);
      never(channel);

      oneOf(pushChannelServiceAsync).openChannel(with(any(AsyncCallback.class)));
      will(retryAction);
      oneOf(channel).open(with("channelToken"), with(channelListener));
    }});

    pushChannel.openChannel();
    asyncAction.onFailure(new RuntimeException());
    retryAction.onSuccess("channelToken");
  }

  @Test
  public void subscribeForEvent() {

    final AsyncAction<Void> asyncAction = new AsyncAction<Void>();

    final SimpleEvent event = new SimpleEvent();
    final StubAsyncSubscribeCallback subscribeCallback = new StubAsyncSubscribeCallback();

    context.checking(new Expectations() {{
      oneOf(pushChannelServiceAsync).subscribe(with(event.getAssociatedType()), with(any(AsyncCallback.class)));
      will(asyncAction);
    }});

    pushChannel.subscribe(event.getAssociatedType(), subscribeCallback);
    asyncAction.onSuccess(null);

    assertThat(subscribeCallback.subscribed, is(true));
  }

  @Test
  public void retrySubscribingForEvent() {

    final AsyncAction<Void> asyncAction = new AsyncAction<Void>();
    final AsyncAction<Void> retryAction = new AsyncAction<Void>();

    final SimpleEvent event = new SimpleEvent();
    final StubAsyncSubscribeCallback subscribeCallback = new StubAsyncSubscribeCallback();

    context.checking(new Expectations() {{
      oneOf(pushChannelServiceAsync).subscribe(with(event.getAssociatedType()), with(any(AsyncCallback.class)));
      will(asyncAction);

      oneOf(pushChannelServiceAsync).subscribe(with(event.getAssociatedType()), with(any(AsyncCallback.class)));
      will(retryAction);
    }});

    pushChannel.subscribe(event.getAssociatedType(), subscribeCallback);
    asyncAction.onFailure(new RuntimeException());
    assertThat(subscribeCallback.subscribed, is(false));

    retryAction.onSuccess(null);
    assertThat(subscribeCallback.subscribed, is(true));
  }

  private class SimpleEvent extends PushEvent<SimpleEventHandler> {

    public Type<SimpleEventHandler> TYPE = new Type<SimpleEventHandler>() {
      @Override
      public String getEventName() {
        return "SimpleEvent";
      }
    };

    @Override
    public Type<SimpleEventHandler> getAssociatedType() {
      return null;
    }

    @Override
    public void dispatch(SimpleEventHandler handler) {
    }
  }

  private class SimpleEventHandler implements PushEventHandler {
  }

  private class StubAsyncSubscribeCallback implements AsyncSubscribeCallback {

    boolean subscribed = false;

    @Override
    public void onSuccess() {
      subscribed = true;
    }
  }
}
