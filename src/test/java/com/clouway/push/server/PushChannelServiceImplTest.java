package com.clouway.push.server;

import com.clouway.push.client.InstanceCapture;
import com.clouway.push.server.util.DateTime;
import com.google.appengine.labs.repackaged.com.google.common.collect.Sets;
import com.google.common.collect.Lists;
import com.google.inject.util.Providers;
import org.jmock.Expectations;
import org.jmock.auto.Mock;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static com.clouway.push.server.Subscription.aNewSubscription;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
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

  private Subscription subscription = aNewSubscription().subscriber(subscriber)
          .eventKey("SimpleEvent")
          .build();

  @Before
  public void setUp() {
    pushChannelService = new PushChannelServiceImpl(Providers.of(repository), Providers.of(subscriptionsExpirationDate));
  }

  @Test
  public void subscribeForEvents() {

    final InstanceCapture<List<Subscription>> subscriptionsCapture = new InstanceCapture<List<Subscription>>();
    SimpleEvent simpleEvent = new SimpleEvent();
    String correlationId = "12345";
    CorrelationEvent correlationEvent = new CorrelationEvent();

    List<String> eventKeys = Lists.newArrayList(simpleEvent.getKey(), correlationEvent.getKey() + correlationId);

    context.checking(new Expectations() {{
      oneOf(repository).put(with(subscriber), with(subscriptionsCapture));
    }});

    pushChannelService.subscribe(subscriber, eventKeys);

    List<Subscription> subscriptions = subscriptionsCapture.getValue();
    assertThat(subscriptions, is(notNullValue()));
    assertThat(subscriptions.size(), is(2));

    Subscription subscription = subscriptions.get(0);
    assertThat(subscription.getSubscriber(), is(subscriber));
    assertThat(subscription.getEventKey(), is("SimpleEvent"));
    assertThat(subscription.getExpirationDate(), is(subscriptionsExpirationDate));

    subscription = subscriptions.get(1);
    assertThat(subscription.getSubscriber(), is(subscriber));
    assertThat(subscription.getEventKey(), is("CorrelationEvent12345"));
    assertThat(subscription.getExpirationDate(), is(subscriptionsExpirationDate));
  }

  @Test
  public void unsubscribeFromSubscribedEvent() {

    context.checking(new Expectations() {{
      oneOf(repository).removeSubscriptions(event.getKey(), Sets.newHashSet(subscriber));
    }});

    pushChannelService.unsubscribe(subscriber, event.getKey());
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

  private class SimpleEvent extends PushEvent {

    private SimpleEvent() {
      super("SimpleEvent");
    }

  }

  private class CorrelationEvent extends PushEvent {

    private CorrelationEvent() {
      super("CorrelationEvent");
    }

  }
}
