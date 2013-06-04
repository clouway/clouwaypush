package com.clouway.push.client;

import com.clouway.push.client.channelapi.Channel;
import com.clouway.push.client.channelapi.ChannelListener;
import com.clouway.push.client.channelapi.PushChannelServiceAsync;
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
    final InstanceMatcher<ChannelListener> channelListener = new InstanceMatcher<ChannelListener>();

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
    final InstanceMatcher<ChannelListener> channelListener = new InstanceMatcher<ChannelListener>();

    context.checking(new Expectations() {{
      oneOf(pushChannelServiceAsync).openChannel(with(any(AsyncCallback.class)));
      will(asyncAction);

      oneOf(channel).open(with("channelToken"), with(channelListener));
    }});

    pushChannel.openChannel();
    asyncAction.onSuccess("channelToken");

    final AsyncAction<String> anotherAsyncAction = new AsyncAction<String>();
    final InstanceMatcher<ChannelListener> anotherChannelListener = new InstanceMatcher<ChannelListener>();

    context.checking(new Expectations() {{
      oneOf(pushChannelServiceAsync).openChannel(with(any(AsyncCallback.class)));
      will(anotherAsyncAction);

      oneOf(channel).open(with("newChannelToken"), with(anotherChannelListener));
    }});

    channelListener.getInstance().onTokenExpire();
    anotherAsyncAction.onSuccess("newChannelToken");
  }

  @Test
  public void retryOpeningNewChannelOnFailure() {

    final AsyncAction<String> asyncAction = new AsyncAction<String>();
    final AsyncAction<String> retryAction = new AsyncAction<String>();
    final InstanceMatcher<ChannelListener> channelListener = new InstanceMatcher<ChannelListener>();

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
}
