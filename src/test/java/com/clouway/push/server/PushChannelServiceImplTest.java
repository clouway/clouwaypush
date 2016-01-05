package com.clouway.push.server;

import com.clouway.push.client.InstanceCapture;
import com.clouway.push.client.channelapi.PushChannelService;
import com.clouway.push.shared.PushEvent;
import com.clouway.push.shared.PushEventHandler;
import com.clouway.push.shared.util.DateTime;
import com.google.appengine.labs.repackaged.com.google.common.collect.Sets;
import com.google.inject.util.Providers;
import org.jmock.Expectations;
import org.jmock.auto.Mock;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static com.clouway.push.server.EventTypeMatcher.isType;
import static com.clouway.push.server.Subscription.aNewSubscription;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author Ivan Lazov <ivan.lazov@clouway.com>
 */
public class PushChannelServiceImplTest {

  @Rule
  public final JUnitRuleMockery context = new JUnitRuleMockery();

  @Mock
  SubscriptionsRepository repository;

  private PushChannelService pushChannelService;

  private final DateTime subscriptionsExpirationDate = new DateTime();
  private final String subscriber = "john@gmail.com";
  private SimpleEvent event = new SimpleEvent();

  private InstanceCapture<Subscription> subscriptionCapture = new InstanceCapture<Subscription>();

  private Subscription subscription = aNewSubscription().subscriber(subscriber)
          .eventName("SimpleEvent")
          .eventType(event.TYPE)
          .build();

  @Before
  public void setUp() {
    pushChannelService = new PushChannelServiceImpl(Providers.of(repository), Providers.of(subscriptionsExpirationDate));
  }

  @Test
  public void subscribeForEvent() {

    context.checking(new Expectations() {{
      oneOf(repository).put(with(subscriptionCapture));
    }});

    pushChannelService.subscribe(subscriber, event.TYPE);

    Subscription subscription = subscriptionCapture.getValue();

    assertThat(subscription.getSubscriber(), is(subscriber));
    assertThat(subscription.getEventName(), is("SimpleEvent"));
    assertThat(subscription.getEventType(), isType(event.getAssociatedType()));
    assertThat(subscription.getExpirationDate(), is(subscriptionsExpirationDate));
  }

  @Test
  public void unsubscribeFromSubscribedEvent() {

    context.checking(new Expectations() {{
      oneOf(repository).removeSubscriptions(event.TYPE, Sets.newHashSet(subscriber));
    }});

    pushChannelService.unsubscribe(subscriber, event.TYPE);
  }

  @Test
  public void keepAliveSubscriberSubscriptions() {

    final List<Subscription> subscriptions = new ArrayList<Subscription>();
    subscriptions.add(subscription);

    context.checking(new Expectations() {{
      oneOf(repository).keepAliveTill(subscriber, subscriptionsExpirationDate);
    }});

    pushChannelService.keepAlive(subscriber);
  }

  private class SimpleEvent extends PushEvent<PushEventHandler> {

    private Type<PushEventHandler> TYPE = new Type<PushEventHandler>("SimpleEvent") {
    };

    @Override
    public Type<PushEventHandler> getAssociatedType() {
      return TYPE;
    }

    @Override
    public void dispatch(PushEventHandler handler) {
    }
  }
}
