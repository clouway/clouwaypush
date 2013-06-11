package com.clouway.push.server;

import com.clouway.push.shared.PushEvent;
import com.clouway.push.client.channelapi.PushChannelService;
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
  private final Provider<DateTime> currentDate;

  @Inject
  public PushChannelServiceImpl(Provider<SubscriptionsRepository> subscriptionsRepository,
                                @CurrentDateAndTime Provider<DateTime> currentDate) {
    this.subscriptionsRepository = subscriptionsRepository.get();
    this.currentDate = currentDate;
  }

  @Override
  public String openChannel(String subscriber) {

    log.info("Open channel for user: " + subscriber);

    return ChannelServiceFactory.getChannelService().createChannel(subscriber);
  }

  @Override
  public void subscribe(String subscriber, PushEvent.Type eventType) {

    log.info("subscribe me for event " + eventType.getEventName() + "   user : "+ subscriber);

    Subscription subscription = aNewSubscription().eventName(eventType.getEventName())
                                                  .eventType(eventType)
                                                  .subscriber(subscriber)
                                                  .expirationDateAndTime(currentDate.get().plusMills(60 * 1000))
                                                  .build();

    subscriptionsRepository.put(subscription);
  }

  @Override
  public void unsubscribe(String subscriber,PushEvent.Type eventType) {

    log.info("Unsubscribe... user: " + subscriber);

    if (subscriptionsRepository.hasSubscription(eventType, subscriber)) {
      subscriptionsRepository.removeSubscription(eventType, subscriber);
    }
  }

  @Override
  public void iAmAlive(String subscriber, int seconds) {

    log.info("Im alive... user: " + subscriber + "   time: " + seconds);

    List<Subscription> subscriptions = subscriptionsRepository.findSubscriptions(subscriber);
    for (Subscription subscription : subscriptions) {
      subscription.renewingTillDate(currentDate.get().plusMills(seconds * 1000));
      subscriptionsRepository.put(subscription);
    }
  }

  @Override
  public void removeSubscriptions(String subscriber) {
    subscriptionsRepository.removeAllSubscriptions(subscriber);
  }

  @Override
  public PushEvent dummyMethod() {
    return null;
  }
}
