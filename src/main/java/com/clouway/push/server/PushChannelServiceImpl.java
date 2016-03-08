package com.clouway.push.server;

import com.clouway.push.server.util.DateTime;
import com.google.appengine.api.channel.ChannelServiceFactory;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.Provider;

import java.util.List;
import java.util.logging.Logger;

import static com.clouway.push.server.Subscription.aNewSubscription;

/**
 * @author Ivan Lazov <ivan.lazov@clouway.com>
 */
class PushChannelServiceImpl implements PushChannelService {

  Logger log = Logger.getLogger(this.getClass().getSimpleName());

  private final SubscriptionsRepository subscriptionsRepository;
  private final Provider<DateTime> expirationDate;

  @Inject
  public PushChannelServiceImpl(Provider<SubscriptionsRepository> subscriptionsRepository,
                                @SubscriptionsExpirationDate Provider<DateTime> subscriptionsExpirationDate) {
    this.subscriptionsRepository = subscriptionsRepository.get();
    this.expirationDate = subscriptionsExpirationDate;
  }

  @Override
  public String connect(String subscriber) {

    log.info("Open channel for subscriber: " + subscriber);

    return ChannelServiceFactory.getChannelService().createChannel(subscriber);
  }

  @Override
  public void subscribe(String subscriber, List<String> keys) {
    List<Subscription> subscriptions = Lists.newArrayList();

    for (String key : keys) {
      log.info("Subscribe: " + subscriber + " for event: " + key);

      subscriptions.add(aNewSubscription()
              .eventKey(key)
              .subscriber(subscriber)
              .expires(expirationDate.get())
              .build());
    }

    subscriptionsRepository.put(subscriber, subscriptions);
  }

  @Override
  public void unsubscribe(String subscriber, String key) {
    log.info("Unsubscribe: " + subscriber + " from event: " + key);
    subscriptionsRepository.removeSubscriptions(key, Sets.newHashSet(subscriber));
  }

  @Override
  public void keepAlive(String subscriber) {

    subscriptionsRepository.keepAliveTill(subscriber, expirationDate.get());
  }
}
