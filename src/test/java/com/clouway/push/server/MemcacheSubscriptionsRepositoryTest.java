package com.clouway.push.server;

import com.clouway.push.shared.PushEvent;
import com.clouway.push.shared.PushEventHandler;
import com.clouway.push.shared.util.DateTime;
import com.google.appengine.api.memcache.MemcacheServiceFactory;
import com.google.appengine.tools.development.testing.LocalMemcacheServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.inject.util.Providers;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static com.clouway.push.server.Subscription.aNewSubscription;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;

/**
 * @author Ivan Lazov <ivan.lazov@clouway.com>
 */
public class MemcacheSubscriptionsRepositoryTest {

  private final LocalServiceTestHelper helper = new LocalServiceTestHelper(new LocalMemcacheServiceTestConfig());

  private SubscriptionsRepository repository;

  private final String subscriber = "john@gmail.com";

  private final Subscription subscription = aNewSubscription().subscriber(subscriber)
          .eventType(SimpleEvent.TYPE)
          .expires(afterOneMinute())
          .build();

  private final Subscription anotherSubscription = aNewSubscription().subscriber(subscriber)
          .eventType(AnotherEvent.TYPE)
          .expires(afterOneMinute())
          .build();

  private final DateTime currentDate = new DateTime();

  @Before
  public void setUp() {
    helper.setUp();

    final Integer subscriptionsExpiration = 10000;

    repository = new MemcacheSubscriptionsRepository(
            MemcacheServiceFactory.getMemcacheService(),
            Providers.of(subscriptionsExpiration),
            Providers.of(currentDate)
    );
  }

  @Test
  public void multipleSubscribersAreSubscribedToSingleEvent() throws Exception {
    storeSubscriptions(
            aNewSubscription().subscriber("peter@gmail.com").eventType(SimpleEvent.TYPE).expires(afterOneMinute()).build(),
            aNewSubscription().subscriber("john@gmail.com").eventType(SimpleEvent.TYPE).expires(afterOneMinute()).build()
    );

    List<Subscription> subscriptions = repository.findSubscriptions(SimpleEvent.TYPE);
    assertThat(subscriptions.size(), is(2));
    assertThat(subscriptions.get(0).getSubscriber(), is(equalTo("john@gmail.com")));
    assertThat(subscriptions.get(0).getEventName(), is(equalTo(SimpleEvent.TYPE.getKey())));

    assertThat(subscriptions.get(1).getSubscriber(), is(equalTo("peter@gmail.com")));
    assertThat(subscriptions.get(1).getEventName(), is(equalTo(SimpleEvent.TYPE.getKey())));
  }

  @Test
  public void noSubscribersAreSubscribedToSingleEvent() throws Exception {
    List<Subscription> subscriptions = repository.findSubscriptions(SimpleEvent.TYPE);
    assertThat(subscriptions.size(), is(0));
  }

  @Test
  public void singleSubscriberIsSubscribedToMultipleEvents() {
    storeSubscriptions(
            aNewSubscription().subscriber("peter@gmail.com").eventType(SimpleEvent.TYPE).expires(afterOneMinute()).build(),
            aNewSubscription().subscriber("peter@gmail.com").eventType(AnotherEvent.TYPE).expires(afterOneMinute()).build()
    );

    List<Subscription> firstSubscriptions = repository.findSubscriptions(SimpleEvent.TYPE);
    List<Subscription> secondSubscriptions = repository.findSubscriptions(AnotherEvent.TYPE);

    assertThat(firstSubscriptions.size(), is(equalTo(1)));
    assertThat(secondSubscriptions.size(), is(equalTo(1)));
  }

  @Test
  public void singleSubscriberIsBulkSubscribedToMultipleEvents() {
    List<Subscription> subscriptions = Lists.newArrayList(
            aNewSubscription().subscriber("peter@gmail.com").eventType(SimpleEvent.TYPE).expires(afterOneMinute()).build(),
            aNewSubscription().subscriber("peter@gmail.com").eventType(AnotherEvent.TYPE).expires(afterOneMinute()).build()
    );

    repository.put("peter@gmail.com", subscriptions);

    List<Subscription> firstSubscriptions = repository.findSubscriptions(SimpleEvent.TYPE);
    List<Subscription> secondSubscriptions = repository.findSubscriptions(AnotherEvent.TYPE);

    assertThat(firstSubscriptions.size(), is(equalTo(1)));
    assertThat(secondSubscriptions.size(), is(equalTo(1)));
  }

  @Test
  public void subscriptionDuplicationIsNotAllowed() throws Exception {
    storeSubscriptions(
            aNewSubscription().subscriber("peter@gmail.com").eventType(SimpleEvent.TYPE).expires(afterOneMinute()).build(),
            aNewSubscription().subscriber("peter@gmail.com").eventType(SimpleEvent.TYPE).expires(afterOneMinute()).build()
    );

    List<Subscription> subscriptions = repository.findSubscriptions(SimpleEvent.TYPE);
    assertThat(subscriptions.size(), is(equalTo(1)));
    assertThat(subscriptions.get(0).getSubscriber(), is(equalTo("peter@gmail.com")));
  }

  @Test
  public void bulkSubscriptionDuplicationIsNotAllowed() throws Exception {
    List<Subscription> duplicateSubscriptions = Lists.newArrayList(
            aNewSubscription().subscriber("peter@gmail.com").eventType(SimpleEvent.TYPE).expires(afterOneMinute()).build(),
            aNewSubscription().subscriber("peter@gmail.com").eventType(SimpleEvent.TYPE).expires(afterOneMinute()).build()
    );

    repository.put("peter@gmail.com", duplicateSubscriptions);

    List<Subscription> subscriptions = repository.findSubscriptions(SimpleEvent.TYPE);
    assertThat(subscriptions.size(), is(equalTo(1)));
    assertThat(subscriptions.get(0).getSubscriber(), is(equalTo("peter@gmail.com")));
  }

  @Test
  public void removeSingleSubscription() throws Exception {

    storeSubscriptions(subscription);

    repository.removeSubscriptions(subscription.getEventType(), Sets.newHashSet(subscription.getSubscriber()));

    List<Subscription> subscriptions = repository.findSubscriptions(subscription.getEventType());
    assertThat(subscriptions.size(), is(0));
  }

  @Test
  public void putTwoSubscriptionsRemoveOnlyOneOfThem() throws Exception {

    storeSubscriptions(subscription, anotherSubscription);

    repository.removeSubscriptions(anotherSubscription.getEventType(), Sets.newHashSet(anotherSubscription.getSubscriber()));

    List<Subscription> simpleEventSubscriptions = repository.findSubscriptions(SimpleEvent.TYPE);
    List<Subscription> anotherEventSubscriptions = repository.findSubscriptions(AnotherEvent.TYPE);

    assertThat(simpleEventSubscriptions.size(), is(1));
    assertThat(anotherEventSubscriptions.size(), is(0));
  }


  @Test
  public void expiredEventSubscriptionsAreNotReturnedForNotification() {
    DateTime fiveMinutesInThePast = currentDate.plusMinutes(-5);

    storeSubscriptions(
            aNewSubscription()
                    .subscriber("user1@user.com")
                    .eventType(SimpleEvent.TYPE)
                    .expires(afterOneMinute())
                    .build(),
            aNewSubscription()
                    .subscriber("user2@user.com")
                    .eventType(SimpleEvent.TYPE)
                    .expires(fiveMinutesInThePast)
                    .build()

    );

    List<Subscription> subscriptions = repository.findSubscriptions(SimpleEvent.TYPE);
    assertThat(subscriptions.size(), is(equalTo(1)));
    assertThat(subscriptions.get(0).getSubscriber(),is("user1@user.com"));
  }

  @Test
  public void removeSingleSubscriptionByEventTypeAndSubscriber() throws Exception {

    storeSubscriptions(
            aNewSubscription()
                    .subscriber("user1@user.com")
                    .eventType(SimpleEvent.TYPE)
                    .expires(afterOneMinute())
                    .build()
    );

    repository.removeSubscriptions(SimpleEvent.TYPE, Sets.<String>newHashSet("user1@user.com"));

    List<Subscription> subscriptions = repository.findSubscriptions(SimpleEvent.TYPE);
    assertThat(subscriptions.size(), is(0));
  }

  @Test
  public void removeSubscribersFromSubscriptionForGivenEventType() throws Exception {

    Subscription firstSubscription = aNewSubscription()
            .subscriber(subscriber)
            .eventType(SimpleEvent.TYPE)
            .expires(afterOneMinute())
            .build();

    Subscription secondSubscription = aNewSubscription()
            .subscriber("anoter@user.com")
            .eventType(SimpleEvent.TYPE)
            .expires(afterOneMinute())
            .build();

    Subscription thirdSubscription = aNewSubscription()
            .subscriber("some@user.com")
            .eventType(SimpleEvent.TYPE)
            .expires(afterOneMinute())
            .build();

    storeSubscriptions(firstSubscription, secondSubscription, thirdSubscription);

    repository.removeSubscriptions(SimpleEvent.TYPE, Sets.newHashSet(subscriber, "some@user.com"));

    List<Subscription> foundSubscriptions = repository.findSubscriptions(SimpleEvent.TYPE);

    assertThat("incorrect subscriber count", foundSubscriptions.size(), is(1));
    assertThat("incorrect subscriber", foundSubscriptions.get(0).getSubscriber(), is("anoter@user.com"));
  }

  @Test
  public void putTwoSubscriptionsRemoveOnlyOneByTypeAndSubscriber() throws Exception {
    storeSubscriptions(subscription, anotherSubscription);

    repository.removeSubscriptions(AnotherEvent.TYPE, Sets.newHashSet(subscriber));

    List<Subscription> subscriptions = repository.findSubscriptions(SimpleEvent.TYPE);
    assertThat(subscriptions.size(), is(1));
  }

  @Test
  public void findSubscriptionAfterRemovingSubscriptionByTypeAndSubscriber() {

    storeSubscriptions(subscription);

    repository.removeSubscriptions(SimpleEvent.TYPE, Sets.newHashSet(subscriber));

    List<Subscription> subscriptions = repository.findSubscriptions(SimpleEvent.TYPE);
    assertThat(subscriptions.size(), is(0));
  }

  @Test
  public void findSubscriptionAfterRemovingGivenSubscription() {
    storeSubscriptions(subscription);

    repository.removeSubscriptions(subscription.getEventType(), Sets.newHashSet(subscription.getSubscriber()));

    List<Subscription> subscriptions = repository.findSubscriptions(SimpleEvent.TYPE);
    assertThat(subscriptions.size(), is(0));
  }

  @Test
  public void updateSubscriptionsExpirationDateWhenStored() {

    subscription.setExpirationDate(afterOneMinute());
    storeSubscriptions(subscription);

    subscription.setExpirationDate(afterOneMinute());
    storeSubscriptions(subscription);

    List<Subscription> subscriptions = repository.findSubscriptions(SimpleEvent.TYPE);
    assertThat(subscriptions.get(0).getExpirationDate(), is(equalTo(subscription.getExpirationDate())));
  }

  @Test
  public void updateSubscriptionsExpirationDateWhenBulkStored() {

    subscription.setExpirationDate(twoMinutesInThePast());
    repository.put(subscriber, Lists.newArrayList(subscription));

    subscription.setExpirationDate(afterOneMinute());
    repository.put(subscriber, Lists.newArrayList(subscription));

    List<Subscription> subscriptions = repository.findSubscriptions(SimpleEvent.TYPE);
    assertThat(subscriptions.get(0).getExpirationDate(), is(equalTo(subscription.getExpirationDate())));
  }

  @Test
  public void keepSessionAlive() {
    Subscription subscription = aNewSubscription()
            .subscriber("john@gmail.com")
            .eventType(SimpleEvent.TYPE)
            .expires(twoMinutesInThePast())
            .build();

    DateTime aliveTime = afterOneMinute();

    repository.put(subscription);
    repository.keepAliveTill("john@gmail.com", aliveTime);

    List<Subscription> eventSubscriptions = repository.findSubscriptions(SimpleEvent.TYPE);
    assertThat(eventSubscriptions.size(), is(equalTo(1)));
    assertThat(eventSubscriptions.get(0).getExpirationDate(), is(equalTo(aliveTime)));
  }

  @Test
  public void keepSessionAliveIsForSingleSubscriber() {

    DateTime oneMinuteInTheFuture = afterOneMinute();
    storeSubscriptions(
            aNewSubscription()
                    .subscriber("some@gmail.com")
                    .eventType(SimpleEvent.TYPE)
                    .expires(oneMinuteInTheFuture)
                    .build(),
            aNewSubscription()
                    .subscriber("another@gmail.com")
                    .eventType(SimpleEvent.TYPE)
                    .expires(oneMinuteInTheFuture)
                    .build()
    );

    DateTime now = new DateTime();

    repository.keepAliveTill("some@gmail.com", now);

    List<Subscription> eventSubscriptions = repository.findSubscriptions(SimpleEvent.TYPE);
    assertThat(eventSubscriptions.size(), is(equalTo(2)));
    assertThat(eventSubscriptions.get(0).getExpirationDate(), is(equalTo(now)));
    assertThat(eventSubscriptions.get(1).getExpirationDate(), is(equalTo(oneMinuteInTheFuture)));
  }

  private DateTime twoMinutesInThePast() {
    return new DateTime().plusMinutes(-2);
  }

  private DateTime afterOneMinute() {
    return new DateTime().plusMinutes(1);
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
