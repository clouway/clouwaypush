package com.clouway.push.server;

import com.clouway.push.shared.PushEvent;
import com.clouway.push.shared.PushEventHandler;
import com.google.appengine.api.memcache.MemcacheServiceFactory;
import com.google.appengine.tools.development.testing.LocalMemcacheServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import static com.clouway.push.server.Subscription.aNewSubscription;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

/**
 * @author Ivan Lazov <ivan.lazov@clouway.com>
 */
public class MemcachSubscriptionsRepositoryTest {

  private final LocalServiceTestHelper helper = new LocalServiceTestHelper(new LocalMemcacheServiceTestConfig());

  private SubscriptionsRepository repository;

  private final String subscriber = "john@gmail.com";
  private final SimpleEvent event = new SimpleEvent();
  private final AnotherEvent anotherEvent = new AnotherEvent();

  private Subscription subscription;

  @Before
  public void setUp() {

    helper.setUp();

    repository = new MemcachSubscriptionsRepository(MemcacheServiceFactory.getMemcacheService());

    subscription = aNewSubscription().subscriber(subscriber).eventType(event.getAssociatedType()).build();
  }

  @Test
  public void putSingleSubscription() throws Exception {

    repository.put(subscription);

    assertTrue(repository.hasSubscription(event.getAssociatedType(), subscriber));
  }

  @Test
  public void putTwoSubscription() throws Exception {

    Subscription anotherSubscription = aNewSubscription().subscriber(subscriber).eventType(anotherEvent.getAssociatedType()).build();
    repository.put(anotherSubscription);
    repository.put(subscription);

    assertTrue(repository.hasSubscription(event.getAssociatedType(), subscriber));
    assertTrue(repository.hasSubscription(anotherEvent.getAssociatedType(), subscriber));
  }

  @Test
  public void putSubscriptionsOfSameEventTypeForTwoSubscribers() throws Exception {

    Subscription anotherSubscription = aNewSubscription().subscriber("me@clouway.com").eventType(event.getAssociatedType()).build();

    repository.put(subscription);
    repository.put(anotherSubscription);

    assertTrue(repository.hasSubscription(event.getAssociatedType(), subscriber));
    assertTrue(repository.hasSubscription(event.getAssociatedType(), "me@clouway.com"));
  }

  @Test
  public void putSubscriptionsOfSameEventTypeForSingleSubscriber() throws Exception {

    Subscription anotherSubscription = aNewSubscription().subscriber(subscriber).eventType(event.getAssociatedType()).build();
    repository.put(anotherSubscription);
    repository.put(subscription);

    assertTrue(repository.hasSubscription(event.getAssociatedType(),subscriber));

    List<Subscription> subscriptions = repository.findSubscriptions(subscriber);
    assertThat(subscriptions.size(),is(equalTo(1)));
  }

  @Test
  public void findSubscriberForEvent() throws Exception {

    repository.put(aNewSubscription().eventType(SimpleEvent.TYPE).subscriber(subscriber).build());
    repository.put(aNewSubscription().eventType(AnotherEvent.TYPE).subscriber(subscriber).build());

    List<Subscription> subscriptions = repository.findSubscriptions(SimpleEvent.TYPE);

    assertThat(subscriptions.size(),is(equalTo(1)));
    assertThat(subscriptions.get(0).getSubscriber(),is(equalTo(subscriber)));
    assertThat(subscriptions.get(0).getEventName(),is(equalTo(SimpleEvent.TYPE.getEventName())));
  }

  @Test
  public void findSubscribersForEvent() throws Exception {

    repository.put(aNewSubscription().eventType(SimpleEvent.TYPE).subscriber(subscriber).build());
    repository.put(aNewSubscription().eventType(AnotherEvent.TYPE).subscriber(subscriber).build());

    repository.put(aNewSubscription().eventType(SimpleEvent.TYPE).subscriber("me@Clouway.com").build());
    repository.put(aNewSubscription().eventType(AnotherEvent.TYPE).subscriber("me@Clouway.com").build());

    List<Subscription> subscriptions = repository.findSubscriptions(SimpleEvent.TYPE);

    assertThat(subscriptions.size(),is(equalTo(2)));
    assertThat(subscriptions.get(1).getSubscriber(),is(equalTo(subscriber)));
    assertThat(subscriptions.get(1).getEventName(),is(equalTo(SimpleEvent.TYPE.getEventName())));
    assertThat(subscriptions.get(0).getSubscriber(),is(equalTo("me@Clouway.com")));
    assertThat(subscriptions.get(0).getEventName(),is(equalTo(SimpleEvent.TYPE.getEventName())));
  }

  @Test
  public void removeSingleSubscription() throws Exception {

    repository.put(subscription);
    repository.removeSubscription(subscription);

    assertFalse(repository.hasSubscription(SimpleEvent.TYPE, subscriber));
  }

  @Test
  public void removeAllSubscriptionsForSubscriber() throws Exception {

    repository.put(aNewSubscription().subscriber(subscriber).eventType(SimpleEvent.TYPE).build());
    repository.put(aNewSubscription().subscriber(subscriber).eventType(AnotherEvent.TYPE).build());

    repository.put(aNewSubscription().subscriber("me@gmail.com").eventType(SimpleEvent.TYPE).build());
    repository.put(aNewSubscription().subscriber("me@gmail.com").eventType(AnotherEvent.TYPE).build());

    repository.removeAllSubscriptions(subscriber);

    assertFalse(repository.hasSubscription(SimpleEvent.TYPE, subscriber));
    assertFalse(repository.hasSubscription(AnotherEvent.TYPE, subscriber));
  }

  @Test
  public void putTwoSubscriptionsRemoveOnlyOneOfThem() throws Exception {

    Subscription anotherSubscription = aNewSubscription().subscriber(subscriber).eventType(AnotherEvent.TYPE).build();
    repository.put(anotherSubscription);
    repository.put(subscription);

    repository.removeSubscription(aNewSubscription().subscriber(subscriber).eventType(AnotherEvent.TYPE).build());

    assertTrue(repository.hasSubscription(SimpleEvent.TYPE, subscriber));
    assertFalse(repository.hasSubscription(AnotherEvent.TYPE, subscriber));
  }

  @Test
  public void removeSingleSubscriptionByEventTypeAndSubscriber() throws Exception {

    repository.put(subscription);
    repository.removeSubscription(SimpleEvent.TYPE, subscriber);

    assertFalse(repository.hasSubscription(SimpleEvent.TYPE, subscriber));
  }

  @Test
  public void removeAllSubscriptionsWhenSubscriberDoNotHaveAny() throws Exception {

    repository.removeAllSubscriptions(subscriber);

    assertFalse(repository.hasSubscription(SimpleEvent.TYPE, subscriber));
  }

  @Test
  public void putTwoSubscriptionsRemoveOneByEventTypeAndSubscriber() throws Exception {

    Subscription anotherSubscription = aNewSubscription().subscriber(subscriber).eventType(AnotherEvent.TYPE).build();
    repository.put(anotherSubscription);
    repository.put(subscription);

    repository.removeSubscription(AnotherEvent.TYPE, subscriber);

    assertTrue(repository.hasSubscription(SimpleEvent.TYPE,subscriber));
    assertFalse(repository.hasSubscription(AnotherEvent.TYPE,subscriber));
  }

  @Test
  public void findSubscriptionByTypeAfterRemovingSubscriptionByEventTypeAndSubscriber() {

    repository.put(subscription);
    repository.removeSubscription(event.getAssociatedType(), subscription.getSubscriber());

    List<Subscription> subscriptions = repository.findSubscriptions(event.getAssociatedType());

    assertThat(subscriptions.size(), is(0));
  }

  @Test
  public void findSubscriptionByTypeAfterRemovingSubscriptionByGivenSubscription() {

    repository.put(subscription);
    repository.removeSubscription(subscription);

    List<Subscription> subscriptions = repository.findSubscriptions(event.getAssociatedType());

    assertThat(subscriptions.size(), is(0));
  }

  @Test
  public void findSubscriptionByTypeAfterRemovingAllSubscriptionsForSubscriber() {

    Subscription anotherSubscription = aNewSubscription().subscriber(subscriber).eventType(anotherEvent.getAssociatedType()).build();
    repository.put(anotherSubscription);
    repository.put(subscription);

    repository.removeAllSubscriptions(subscriber);

    List<Subscription> subscriptions = repository.findSubscriptions(event.getAssociatedType());
    List<Subscription> anotherSubscriptions = repository.findSubscriptions(anotherEvent.getAssociatedType());

    assertThat(subscriptions.size(), is(0));
    assertThat(anotherSubscriptions.size(), is(0));
  }

  private interface AnotherEventHandler extends PushEventHandler {
  }

  private static class AnotherEvent extends PushEvent<AnotherEventHandler> {

    public static Type<AnotherEventHandler> TYPE = new Type<AnotherEventHandler>("AnotherEvent");

    @Override
    public Type<AnotherEventHandler> getAssociatedType() {
      return TYPE;
    }

    @Override
    public void dispatch(AnotherEventHandler handler) {
    }
  }

  private interface SimpleEventHandler extends PushEventHandler {
  }

  private static class SimpleEvent extends PushEvent<SimpleEventHandler> {

    public static Type<SimpleEventHandler> TYPE = new Type<SimpleEventHandler>("SimpleEvent");

    @Override
    public PushEvent.Type<SimpleEventHandler> getAssociatedType() {
      return TYPE;
    }

    @Override
    public void dispatch(SimpleEventHandler handler) {
    }
  }
}
