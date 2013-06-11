package com.clouway.push.server;

import com.clouway.push.shared.PushEvent;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Inject;

import java.util.List;
import java.util.Map;

import static com.clouway.push.server.Subscription.aNewSubscription;

/**
 * @author Ivan Lazov <ivan.lazov@clouway.com>
 */
public class MemcachSubscriptionsRepository implements SubscriptionsRepository {

  private final MemcacheService memcacheService;

  @Inject
  public MemcachSubscriptionsRepository(MemcacheService memcacheService) {
    this.memcacheService = memcacheService;
  }

  @Override
  public boolean hasSubscription(PushEvent.Type eventType, String subscriber) {

    Map<String, Subscription> subscriptionsMap = fetchSubscriptions(subscriber);

    return subscriptionsMap != null && subscriptionsMap.containsKey(eventType.getEventName());
  }

  @Override
  public void put(Subscription subscription) {

    storeAndUpdateSubscriberSubscriptions(subscription);

    storeAndUpdateEventSubscriptions(subscription);
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

  @Override
  public void removeSubscription(PushEvent.Type eventType, String subscriber) {
    removeSubscription(aNewSubscription().eventType(eventType).subscriber(subscriber).build());
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

    Map<String, Subscription> subscriptions = (Map<String, Subscription>) memcacheService.get(type.getEventName());

    if (subscriptions != null) {
      return Lists.newArrayList(subscriptions.values());
    }

    return Lists.newArrayList();
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

  private void storeSubscriptions(String subscriber, Map<String, Subscription> subscriptionMap) {
    safeStore(subscriber, subscriptionMap);
  }

  private void storeSubscriptions(PushEvent.Type type, Map<String, Subscription> subscriptionMap) {
    safeStore(type.getEventName(), subscriptionMap);
  }

  private void safeStore(String key, Map<String, Subscription> subscriptionMap) {

    MemcacheService.IdentifiableValue identifiableValue = memcacheService.getIdentifiable(key);
    if (identifiableValue != null) {
      memcacheService.putIfUntouched(key, identifiableValue, subscriptionMap);
    } else {
      memcacheService.put(key, subscriptionMap);
    }
  }

  private Map<String, Subscription> fetchSubscriptions(String subscriber) {
    return safeGet(subscriber);
  }

  private Map<String, Subscription> fetchSubscriptions(PushEvent.Type type) {
    return safeGet(type.getEventName());
  }

  private Map<String, Subscription> safeGet(String key) {

    MemcacheService.IdentifiableValue identifiableValue = memcacheService.getIdentifiable(key);
    if (identifiableValue != null) {
      return (Map<String, Subscription>) identifiableValue.getValue();
    }

    return (Map<String, Subscription>) memcacheService.get(key);
  }
}
