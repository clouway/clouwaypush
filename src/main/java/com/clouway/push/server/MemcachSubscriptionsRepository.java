package com.clouway.push.server;

import com.clouway.push.shared.PushEvent;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.util.List;
import java.util.Map;

/**
 * @author Ivan Lazov <ivan.lazov@clouway.com>
 */
public class MemcachSubscriptionsRepository implements SubscriptionsRepository {

  private MemcacheService syncCache;

  public MemcachSubscriptionsRepository() {
    syncCache = MemcacheServiceFactory.getMemcacheService();
  }

  @Override
  public boolean hasSubscription(PushEvent.Type eventType, String subscriber) {
    Map<String, Subscription> subscriptions = findSubscriptionsMap(subscriber);
    Map<String, Subscription> eventSubscriptions = findSubscriptionsMap(eventType);
    return subscriptions.containsKey(eventType.getEventName()) && eventSubscriptions.containsKey(subscriber);
  }

  @Override
  public void put(Subscription subscription) {

    Map<String, Subscription> subscriptions = findSubscriptionsMap(subscription.getSubscriber());
    if (subscriptions == null) {
      subscriptions = Maps.newHashMap();
    }
    subscriptions.put(subscription.getEventName(), subscription);
    putSubscriptions(subscription.getSubscriber(), subscriptions);


    subscriptions = findSubscriptionsMap(subscription.getEventType());
    if (subscriptions == null) {
      subscriptions = Maps.newHashMap();
    }
    subscriptions.put(subscription.getSubscriber(), subscription);
    putSubscriptions(subscription.getEventType(), subscriptions);
  }

  private void putSubscriptions(PushEvent.Type eventType, Map<String, Subscription> subscriptions) {
    syncCache.put("eventType_" + eventType.getEventName(), subscriptions);
  }

  private void putSubscriptions(String subscriber, Map<String, Subscription> subscriptions) {
    syncCache.put("subscriber_" + subscriber, subscriptions);
  }

  @Override
  public Subscription get(PushEvent.Type eventType, String subscriber) {
    Map<String, Subscription> subscriptions = findSubscriptionsMap(subscriber);
    return subscriptions.get(eventType.getEventName());
  }

  @Override
  public void removeSubscription(PushEvent.Type eventType, String subscriber) {
    Map<String, Subscription> subscriptions = findSubscriptionsMap(subscriber);

    if (subscriptions.containsKey(eventType.getEventName())) {
      subscriptions.remove(eventType.getEventName());
      putSubscriptions(subscriber, subscriptions);
    }

    subscriptions = findSubscriptionsMap(eventType);
    if (subscriptions.containsKey(subscriber)) {
      subscriptions.remove(subscriber);
      putSubscriptions(eventType, subscriptions);
    }
  }

  @Override
  public List<Subscription> findSubscriptions(String subscriber) {
    Map<String, Subscription> map = findSubscriptionsMap(subscriber);
    if(map != null){
      return Lists.newArrayList(map.values());
    }

    return Lists.newArrayList();
  }

  public Map<String, Subscription> findSubscriptionsMap(String subscriber) {
    return (Map<String, Subscription>) syncCache.get("subscriber_" + subscriber);
  }

  @Override
  public List<Subscription> findSubscriptions(PushEvent.Type type) {
    Map<String, Subscription> map = findSubscriptionsMap(type);
    if(map != null){
      return Lists.newArrayList(map.values());
    }

    return Lists.newArrayList();
  }

  public Map<String, Subscription> findSubscriptionsMap(PushEvent.Type type) {
    return (Map<String, Subscription>) syncCache.get("eventType_" + type.getEventName());
  }

  @Override
  public void removeSubscription(Subscription subscription) {
    removeSubscription(subscription.getEventType(), subscription.getSubscriber());
  }

  @Override
  public void removeAllSubscriptions(String subscriber) {
    List<Subscription> subscriptions = findSubscriptions(subscriber);

    for (Subscription subscription : subscriptions) {
      Map<String, Subscription> eventSubscriptions = findSubscriptionsMap(subscription.getEventType());
      if (eventSubscriptions.containsKey(subscriber)) {
        eventSubscriptions.remove(subscriber);
        putSubscriptions(subscription.getEventType(), eventSubscriptions);
      }
    }

    putSubscriptions(subscriber, null);
  }
}
