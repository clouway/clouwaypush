package com.clouway.push.server;

import com.clouway.push.client.channelapi.PushChannelService;
import com.clouway.push.shared.PushEvent;
import com.clouway.push.shared.util.DateTime;
import com.google.appengine.api.channel.ChannelServiceFactory;
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
public class PushChannelServiceImpl extends RemoteServiceServlet implements PushChannelService {

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
  public void subscribe(String subscriber, PushEvent.Type type) {

    log.info("Subscribe: " + subscriber + " for event: " + type.getEventName());

    Subscription subscription = aNewSubscription().eventName(type.getEventName())
                                                  .eventType(type)
                                                  .subscriber(subscriber)
                                                  .expirationDate(expirationDate.get())
                                                  .build();

    subscriptionsRepository.put(subscription);
  }

  @Override
  public void unsubscribe(String subscriber, PushEvent.Type eventType) {

    log.info("Unsubscribe: " + subscriber + " from event: " + eventType.getEventName());

    if (subscriptionsRepository.hasSubscription(eventType, subscriber)) {
      subscriptionsRepository.removeSubscription(eventType, subscriber);
    }
  }

  @Override
  public void keepAlive(String subscriber) {

    log.info("Keep alive subscriber: " + subscriber);

    List<Subscription> subscriptions = subscriptionsRepository.findSubscriptions(subscriber);
    for (Subscription subscription : subscriptions) {
      subscription.renewingTillDate(expirationDate.get());
      subscriptionsRepository.put(subscription);
    }
  }

  @Override
  public PushEvent dummyMethod() {
    return null;
  }
}
