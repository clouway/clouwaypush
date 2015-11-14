package com.clouway.push.server;

import com.clouway.push.shared.PushEvent;
import com.clouway.push.shared.util.DateTime;
import com.google.appengine.api.memcache.Expiration;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

/**
 * @author Ivan Lazov <ivan.lazov@clouway.com>
 */
public class MemcacheSubscriptionsRepository implements SubscriptionsRepository {

  private static final Logger log = Logger.getLogger(MemcacheSubscriptionsRepository.class.getName());

  private final MemcacheService memcacheService;
  private Provider<Integer> subscriptionsExpiration;
  private final Provider<DateTime> currentDate;

  @Inject
  public MemcacheSubscriptionsRepository(@Named("MemcacheService") MemcacheService memcacheService,
                                         @SubscriptionsExpirationMills Provider<Integer> subscriptionsExpiration,
                                         @CurrentDate Provider<DateTime> currentDate) {
    this.memcacheService = memcacheService;
    this.subscriptionsExpiration = subscriptionsExpiration;
    this.currentDate = currentDate;
  }


  @Override
  public void put(Subscription subscription) {

    storeAndUpdateSubscriberSubscriptions(subscription);

    storeAndUpdateEventSubscriptions(subscription);
  }

  @Override
  public void removeSubscriptions(PushEvent.Type type, Set<String> subscribers) {
    Map<String, Subscription> subscriptions = fetchSubscriptions(type);

    for (String subscriber : subscribers) {
      subscriptions.remove(subscriber);
    }

    if (!subscribers.isEmpty()) {
      storeSubscriptions(type, subscriptions);
    }
  }

  @Override
  public List<Subscription> findSubscriptions(String subscriber) {

    Map<String, Subscription> subscriptions = fetchSubscriptions(subscriber);
    if (subscriptions != null) {
      return Lists.newArrayList(subscriptions.values());
    }

    return Lists.newArrayList();
  }

  @Override
  public List<Subscription> findSubscriptions(PushEvent.Type type) {
    log.info("Event type: " + type.getKey());

    Map<String, Subscription> subscriptions = getSubscriptions(type.getKey());

    if (subscriptions == null) {
      return Lists.newArrayList();
    }

    Set<String> subscribersForRemove = Sets.newHashSet();
    List<Subscription> activeSubscriptions = Lists.newArrayList();

    DateTime now = currentDate.get();

    for (Subscription subscription : subscriptions.values()) {
      if (subscription.isActive(now)) {
        activeSubscriptions.add(subscription);
      } else {
        subscribersForRemove.add(subscription.getSubscriber());
      }
    }

    removeSubscriptions(type, subscribersForRemove);

    return activeSubscriptions;
  }

  @Override
  public void removeSubscription(Subscription subscription) {

    Map<String, Subscription> subscriptions = fetchSubscriptions(subscription.getSubscriber());

    if (subscriptions != null && subscriptions.containsKey(subscription.getEventName())) {
      subscriptions.remove(subscription.getEventName());
      storeSubscriptions(subscription.getSubscriber(), subscriptions);
    }

    subscriptions = fetchSubscriptions(subscription.getEventType());

    if (subscriptions != null && subscriptions.containsKey(subscription.getSubscriber())) {
      subscriptions.remove(subscription.getSubscriber());
      storeSubscriptions(subscription.getEventType(), subscriptions);
    }
  }

  @Override
  public void removeAllSubscriptions(String subscriber) {

    List<Subscription> subscriptions = findSubscriptions(subscriber);

    for (Subscription subscription : subscriptions) {
      removeSubscription(subscription);
    }
  }

  private void storeAndUpdateSubscriberSubscriptions(Subscription subscription) {

    Map<String, Subscription> subscriberSubscriptions = fetchSubscriptions(subscription.getSubscriber());

    if (subscriberSubscriptions != null) {
      subscriberSubscriptions.put(subscription.getEventName(), subscription);
    } else {
      subscriberSubscriptions = Maps.newHashMap();
      subscriberSubscriptions.put(subscription.getEventName(), subscription);
    }

    storeSubscriptions(subscription.getSubscriber(), subscriberSubscriptions);
  }

  private void storeAndUpdateEventSubscriptions(Subscription subscription) {

    Map<String, Subscription> eventSubscriptions = fetchSubscriptions(subscription.getEventType());
    if (eventSubscriptions != null) {
      eventSubscriptions.put(subscription.getSubscriber(), subscription);
    } else {
      eventSubscriptions = Maps.newHashMap();
      eventSubscriptions.put(subscription.getSubscriber(), subscription);
    }
    storeSubscriptions(subscription.getEventType(), eventSubscriptions);
  }

  private void storeSubscriptions(String subscriber, Map<String, Subscription> subscriptionMap) {
    safeStore(subscriber, subscriptionMap);
  }

  private void storeSubscriptions(PushEvent.Type type, Map<String, Subscription> subscriptionMap) {
    safeStore(type.getKey(), subscriptionMap);
  }

  private void safeStore(String key, Map<String, Subscription> subscriptionMap) {

    MemcacheService.IdentifiableValue identifiableValue = memcacheService.getIdentifiable(key);
    if (identifiableValue != null) {
      memcacheService.putIfUntouched(key, identifiableValue, subscriptionMap, Expiration.byDeltaMillis(subscriptionsExpiration.get()));
    } else {
      memcacheService.put(key, subscriptionMap, Expiration.byDeltaMillis(subscriptionsExpiration.get()));
    }
  }

  private Map<String, Subscription> fetchSubscriptions(String subscriber) {
    return getSubscriptions(subscriber);
  }

  private Map<String, Subscription> fetchSubscriptions(PushEvent.Type type) {
    return getSubscriptions(type.getKey());
  }

  @SuppressWarnings("unchecked")
  private Map<String, Subscription> getSubscriptions(String key) {
    return (Map<String, Subscription>) memcacheService.get(key);
  }


}
