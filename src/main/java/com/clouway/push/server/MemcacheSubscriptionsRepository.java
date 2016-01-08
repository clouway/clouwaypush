package com.clouway.push.server;

import com.clouway.push.shared.PushEvent;
import com.clouway.push.shared.util.DateTime;
import com.google.appengine.api.memcache.Expiration;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheService.CasValues;
import com.google.appengine.api.memcache.MemcacheService.IdentifiableValue;
import com.google.appengine.repackaged.com.google.api.client.util.Maps;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Logger;

/**
 * @author Ivan Lazov <ivan.lazov@clouway.com>
 */
class MemcacheSubscriptionsRepository implements SubscriptionsRepository {

  private static final int MAX_RETRIES = 5;

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
  public void put(final Subscription subscription) {
    safeStoreOrUpdate(subscription.getSubscriber(), new Function<Map<String, Subscription>, Map<String, Subscription>>() {
      @Override
      public Map<String, Subscription> apply(Map<String, Subscription> input) {
        if (input == null) {
          input = Maps.newHashMap();
        }

        input.put(subscription.getEventName(), subscription);
        return input;
      }
    });

    safeStoreOrUpdate(subscription.getEventType().getKey(), new Function<Map<String, Subscription>, Map<String, Subscription>>() {
      @Override
      public Map<String, Subscription> apply(Map<String, Subscription> input) {
        if (input == null) {
          input = Maps.newHashMap();
        }
        input.put(subscription.getSubscriber(), subscription);
        return input;
      }
    });
  }

  @Override
  public void removeSubscriptions(final PushEvent.Type type, final Set<String> subscribers) {

    safeStoreOrUpdate(type.getKey(), new Function<Map<String, Subscription>, Map<String, Subscription>>() {
      @Override
      public Map<String, Subscription> apply(Map<String, Subscription> input) {
        if (input == null) {
          return Maps.newHashMap();
        }
        for (String each : subscribers) {
          input.remove(each);
        }
        return input;
      }
    });
  }

  @Override
  public List<Subscription> findSubscriptions(PushEvent.Type type) {
    log.info("Event type: " + type.getKey());

    final DateTime now = currentDate.get();

    Map<String, Subscription> subscriptions = (Map<String, Subscription>) memcacheService.get(type.getKey());

    for (Entry<String, Subscription> each : subscriptions.entrySet()) {

      // Keep alive ensures that these entries will be deleted.
      if (!each.getValue().isActive(now)) {
        subscriptions.remove(each.getKey());
      }
    }

    return Lists.newArrayList(subscriptions.values());
  }

  @Override
  @SuppressWarnings("unchecked")
  public void keepAliveTill(final String subscriber, final DateTime time) {
    final Set<String> events = Sets.newHashSet();

    safeStoreOrUpdate(subscriber, new Function<Map<String, Subscription>, Map<String, Subscription>>() {
      @Override
      public Map<String, Subscription> apply(Map<String, Subscription> input) {
        if (input == null) {
          return Maps.newHashMap();
        }
        for (Subscription subscription : input.values()) {
          subscription.renewingTillDate(time);
          events.add(subscription.getEventType().getKey());
        }

        return input;
      }
    });

    bulkUpdate(events, new Function<Map<String, Subscription>, Map<String, Subscription>>() {
      @Override
      public Map<String, Subscription> apply(Map<String, Subscription> input) {
        for (Subscription s : input.values()) {
          // Only subscription of requested subscriber is updated.
          if (s.getSubscriber().equals(subscriber)) {
            s.renewingTillDate(time);
          }
        }
        return input;
      }
    });

  }

  @SuppressWarnings("unchecked")
  private Map<String, Subscription> getSubscriptions(String key) {
    return (Map<String, Subscription>) memcacheService.get(key);
  }

  @SuppressWarnings("unchecked")
  private <K, V> V safeStoreOrUpdate(K key, Function<V, V> func) {
    return this.safeStoreOrUpdate(MAX_RETRIES, subscriptionsExpiration.get(), key, func);
  }

  //TODO(mgenov): extract these operation to external object
  @SuppressWarnings("unchecked")
  private <K, V> V safeStoreOrUpdate(int maxRetryCount, int cacheTime, K key, Function<V, V> func) {
    for (int retry = 0; retry < maxRetryCount; retry++) {
      IdentifiableValue identifiables = memcacheService.getIdentifiable(key);

      if (identifiables == null) {
        V result = func.apply(null);
        memcacheService.put(key, result);
        return result;
      }

      V updated = func.apply((V) identifiables.getValue());
      boolean isStored = memcacheService.putIfUntouched(key, identifiables, updated, Expiration.byDeltaMillis(cacheTime));
      if (isStored) {
        return updated;
      }

      // Wait a little bit before next retry
      try {
        Thread.sleep(5);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }

    throw new UpdateFailedException(String.format("The update of key %s was failed due timeout.", key));
  }

  private <K, V> Map<K, V> bulkUpdate(Collection<K> keys, Function<V, V> func) {
    return this.bulkUpdate(MAX_RETRIES, subscriptionsExpiration.get(), keys, func);
  }

  @SuppressWarnings("unchecked")
  private <K, V> Map<K, V> bulkUpdate(int maxRetryCount, int cacheTime, Collection<K> keys, Function<V, V> func) {
    for (int retry = 0; retry < maxRetryCount; retry++) {
      Map<K, IdentifiableValue> identifiables = memcacheService.getIdentifiables(keys);

      if (identifiables.size() == 0) {
        return null;
      }

      Map<K, CasValues> updateMap = Maps.newHashMap();
      Map<K, V> result = Maps.newHashMap();

      for (K each : identifiables.keySet()) {
        V v = (V) identifiables.get(each).getValue();
        result.put(each, func.apply(v));

        updateMap.put(each, new CasValues(identifiables.get(each), v));
      }

      Set<K> updated = memcacheService.putIfUntouched(updateMap, Expiration.byDeltaMillis(cacheTime));

      // If all items are updated, we tell the caller that everything is fine.
      if (updated.size() == identifiables.size()) {
        return result;
      }

      // Wait a little bit before next retry
      try {
        Thread.sleep(5);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }

    throw new UpdateFailedException(String.format("The update of keys %s was failed due timeout.", keys));
  }


}
