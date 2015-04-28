package com.clouway.push.server;

import com.clouway.push.shared.PushEvent;
import com.clouway.push.shared.PushEventHandler;
import com.clouway.push.shared.util.DateTime;
import com.google.appengine.api.memcache.MemcacheServiceFactory;
import com.google.appengine.tools.development.testing.LocalMemcacheServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.common.collect.Sets;
import com.google.inject.util.Providers;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static com.clouway.push.server.Subscription.aNewSubscription;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * @author Ivan Lazov <ivan.lazov@clouway.com>
 */
public class MemcacheSubscriptionsRepositoryTest {

  private final LocalServiceTestHelper helper = new LocalServiceTestHelper(new LocalMemcacheServiceTestConfig());

  private SubscriptionsRepository repository;

  private final String subscriber = "john@gmail.com";

  private final Integer subscriptionsExpiration = 10000;

  private final Subscription subscription = aNewSubscription().subscriber(subscriber)
                                                              .eventType(SimpleEvent.TYPE)
                                                              .build();

  Subscription anotherSubscription = aNewSubscription().subscriber(subscriber)
                                                       .eventType(AnotherEvent.TYPE)
                                                       .build();

  @Before
  public void setUp() {

    helper.setUp();

    repository = new MemcacheSubscriptionsRepository(MemcacheServiceFactory.getMemcacheService(), Providers.of(subscriptionsExpiration));
  }

  @Test
  public void putSingleSubscription() throws Exception {

    storeSubscriptions(subscription);

    assertTrue(repository.hasSubscription(SimpleEvent.TYPE, subscriber));
  }

  @Test
  public void putTwoSubscriptions() throws Exception {

    storeSubscriptions(subscription, anotherSubscription);

    assertTrue(repository.hasSubscription(SimpleEvent.TYPE, subscriber));
    assertTrue(repository.hasSubscription(AnotherEvent.TYPE, subscriber));
  }

  @Test
  public void putSubscriptionsOfSameEventTypeForTwoSubscribers() throws Exception {

    Subscription anotherSubscription = aNewSubscription().subscriber("peter@gmail.com").eventType(SimpleEvent.TYPE).build();

    storeSubscriptions(subscription, anotherSubscription);

    assertTrue(repository.hasSubscription(SimpleEvent.TYPE, subscriber));
    assertTrue(repository.hasSubscription(SimpleEvent.TYPE, "peter@gmail.com"));
  }

  @Test
  public void putSubscriptionsOfSameEventTypeForSingleSubscriber() throws Exception {

    Subscription anotherSubscription = aNewSubscription().subscriber(subscriber).eventType(SimpleEvent.TYPE).build();

    storeSubscriptions(subscription, anotherSubscription);

    List<Subscription> subscriptions = repository.findSubscriptions(subscriber);
    assertThat(subscriptions.size(), is(equalTo(1)));
  }

  @Test
  public void findSubscriberForEvent() throws Exception {

    storeSubscriptions(subscription, anotherSubscription);

    List<Subscription> subscriptions = repository.findSubscriptions(SimpleEvent.TYPE);

    assertThat(subscriptions.size(), is(equalTo(1)));
    assertThat(subscriptions.get(0).getSubscriber(), is(equalTo(subscriber)));
    assertThat(subscriptions.get(0).getEventName(), is(equalTo(SimpleEvent.TYPE.getKey())));
  }

  @Test
  public void findSubscribersForEvent() throws Exception {

    storeSubscriptions(subscription, anotherSubscription,
            aNewSubscription().eventType(SimpleEvent.TYPE).subscriber("peter@gmail.com").build(),
            aNewSubscription().eventType(AnotherEvent.TYPE).subscriber("peter@gmail.com").build());

    List<Subscription> subscriptions = repository.findSubscriptions(SimpleEvent.TYPE);

    assertThat(subscriptions.size(), is(equalTo(2)));

    assertThat(subscriptions.get(0).getSubscriber(), is(equalTo(subscriber)));
    assertThat(subscriptions.get(0).getEventName(), is(equalTo(SimpleEvent.TYPE.getKey())));

    assertThat(subscriptions.get(1).getSubscriber(), is(equalTo("peter@gmail.com")));
    assertThat(subscriptions.get(1).getEventName(), is(equalTo(SimpleEvent.TYPE.getKey())));
  }

  @Test
  public void removeSingleSubscription() throws Exception {

    storeSubscriptions(subscription);

    repository.removeSubscription(subscription);

    assertFalse(repository.hasSubscription(SimpleEvent.TYPE, subscriber));
  }

  @Test
  public void removeAllSubscriptionsForSubscriber() throws Exception {

    storeSubscriptions(subscription, anotherSubscription,
            aNewSubscription().subscriber("peter@gmail.com").eventType(SimpleEvent.TYPE).build(),
            aNewSubscription().subscriber("peter@gmail.com").eventType(AnotherEvent.TYPE).build());

    repository.removeAllSubscriptions(subscriber);

    assertFalse(repository.hasSubscription(SimpleEvent.TYPE, subscriber));
    assertFalse(repository.hasSubscription(AnotherEvent.TYPE, subscriber));

    assertTrue(repository.hasSubscription(SimpleEvent.TYPE, "peter@gmail.com"));
    assertTrue(repository.hasSubscription(AnotherEvent.TYPE, "peter@gmail.com"));
  }

  @Test
  public void putTwoSubscriptionsRemoveOnlyOneOfThem() throws Exception {

    storeSubscriptions(subscription, anotherSubscription);

    repository.removeSubscription(anotherSubscription);

    assertTrue(repository.hasSubscription(SimpleEvent.TYPE, subscriber));
    assertFalse(repository.hasSubscription(AnotherEvent.TYPE, subscriber));
  }

  @Test
  public void removeSingleSubscriptionByEventTypeAndSubscriber() throws Exception {

    storeSubscriptions(subscription);

    repository.removeSubscription(SimpleEvent.TYPE, subscriber);

    assertFalse(repository.hasSubscription(SimpleEvent.TYPE, subscriber));
  }

  @Test
  public void removeSubscribersFromSubscriptionForGivenEventType() throws Exception {

    Subscription firstSubscription = aNewSubscription()
            .subscriber(subscriber)
            .eventType(SimpleEvent.TYPE)
            .build();

    Subscription secondSubscription = aNewSubscription()
            .subscriber("anoter@user.com")
            .eventType(SimpleEvent.TYPE)
            .build();

    Subscription thirdSubscription = aNewSubscription()
            .subscriber("some@user.com")
            .eventType(SimpleEvent.TYPE)
            .build();

    storeSubscriptions(firstSubscription, secondSubscription, thirdSubscription);

    repository.removeSubscriptions(SimpleEvent.TYPE, Sets.newHashSet(subscriber, "some@user.com"));

    List<Subscription> foundSubscriptions = repository.findSubscriptions(SimpleEvent.TYPE);

    assertThat("incorrect subscriber count", foundSubscriptions.size(), is(1));
    assertThat("incorrect subscriber", foundSubscriptions.get(0).getSubscriber(), is("anoter@user.com"));
  }

  @Test
  public void removeAllSubscriptionsWhenSubscriberDoNotHaveAny() throws Exception {

    repository.removeAllSubscriptions(subscriber);

    assertFalse(repository.hasSubscription(SimpleEvent.TYPE, subscriber));
  }

  @Test
  public void putTwoSubscriptionsRemoveOnlyOneByTypeAndSubscriber() throws Exception {

    storeSubscriptions(subscription, anotherSubscription);

    repository.removeSubscription(AnotherEvent.TYPE, subscriber);

    assertTrue(repository.hasSubscription(SimpleEvent.TYPE, subscriber));
    assertFalse(repository.hasSubscription(AnotherEvent.TYPE, subscriber));
  }

  @Test
  public void findSubscriptionAfterRemovingSubscriptionByTypeAndSubscriber() {

    storeSubscriptions(subscription);

    repository.removeSubscription(SimpleEvent.TYPE, subscriber);

    List<Subscription> subscriptions = repository.findSubscriptions(SimpleEvent.TYPE);
    assertThat(subscriptions.size(), is(0));
  }

  @Test
  public void findSubscriptionAfterRemovingGivenSubscription() {

    storeSubscriptions(subscription);

    repository.removeSubscription(subscription);

    List<Subscription> subscriptions = repository.findSubscriptions(SimpleEvent.TYPE);
    assertThat(subscriptions.size(), is(0));
  }

  @Test
  public void findSubscriptionAfterRemovingAllSubscriptionForSubscriber() {

    storeSubscriptions(subscription, anotherSubscription);

    repository.removeAllSubscriptions(subscriber);

    assertFalse(repository.hasSubscription(SimpleEvent.TYPE, subscriber));
    assertFalse(repository.hasSubscription(AnotherEvent.TYPE, subscriber));
  }

  @Test
  public void updateSubscriptionsExpirationDateWhenStored() {

    subscription.setExpirationDate(new DateTime());
    storeSubscriptions(subscription);

    DateTime newDate = new DateTime().plusMinutes(1);
    subscription.setExpirationDate(newDate);

    storeSubscriptions(subscription);

    List<Subscription> subscriptions = repository.findSubscriptions(SimpleEvent.TYPE);
    assertThat(subscriptions.get(0).getExpirationDate(), is(equalTo(newDate)));
  }

  private void storeSubscriptions(Subscription... subscriptions) {
    for (Subscription subscription : subscriptions) {
      repository.put(subscription);
    }
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
