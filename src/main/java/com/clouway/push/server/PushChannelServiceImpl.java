package com.clouway.push.server;

import com.clouway.push.client.channelapi.PushChannelService;
import com.clouway.push.shared.PushEvent;
import com.clouway.push.shared.util.DateTime;
import com.google.appengine.api.channel.ChannelServiceFactory;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

import java.util.List;
import java.util.logging.Logger;

import static com.clouway.push.server.Subscription.aNewSubscription;

/**
 * @author Ivan Lazov <ivan.lazov@clouway.com>
 */
@Singleton
class PushChannelServiceImpl extends RemoteServiceServlet implements PushChannelService {

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
  public void subscribe(String subscriber, List<PushEvent.Type> types) {
    List<Subscription> subscriptions = Lists.newArrayList();

    for (PushEvent.Type type : types) {
      log.info("Subscribe: " + subscriber + " for event: " + type.getKey());

      subscriptions.add(aNewSubscription().eventName(type.getKey())
              .eventType(type)
              .subscriber(subscriber)
              .expires(expirationDate.get())
              .build());
    }

    subscriptionsRepository.put(subscriber, subscriptions);
  }

  @Override
  public void unsubscribe(String subscriber, PushEvent.Type eventType) {
    log.info("Unsubscribe: " + subscriber + " from event: " + eventType.getKey());
    subscriptionsRepository.removeSubscriptions(eventType, Sets.newHashSet(subscriber));
  }

  @Override
  public void keepAlive(String subscriber) {

    subscriptionsRepository.keepAliveTill(subscriber, expirationDate.get());
  }

  @Override
  public PushEvent dummyMethod() {
    return null;
  }
}
