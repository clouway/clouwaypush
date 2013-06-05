package com.clouway.push.server;

import com.clouway.push.shared.PushEvent;
import com.clouway.push.client.channelapi.PushChannelService;
import com.google.appengine.api.channel.ChannelServiceFactory;
import com.google.appengine.repackaged.org.joda.time.DateTime;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

import static com.clouway.push.server.Subscription.aNewSubscription;

/**
 * @author Ivan Lazov <ivan.lazov@clouway.com>
 */
@Singleton
public class PushChannelServiceImpl extends RemoteServiceServlet implements PushChannelService {

  private final Provider<SubscriptionsRepository> subscriptionsRepository;
  private final Provider<Subscriber> subscriber;
  private final Provider<DateTime> currentDateAndTime;

  @Inject
  public PushChannelServiceImpl(Provider<SubscriptionsRepository> subscriptionsRepository, Provider<Subscriber> subscriber, Provider<DateTime> currentDateAndTime) {
    this.subscriptionsRepository = subscriptionsRepository;
    this.subscriber = subscriber;
    this.currentDateAndTime = currentDateAndTime;
  }

  @Override
  public String openChannel() {
    return ChannelServiceFactory.getChannelService().createChannel(subscriber.get().getName());
  }

  @Override
  public void subscribe(PushEvent.Type eventType) {

    String subscriberName = subscriber.get().getName();

    if (!subscriptionsRepository.get().hasSubscription(eventType, subscriberName)) {

      Subscription subscription = aNewSubscription().eventName(eventType.getEventName())
                                                    .subscriber(subscriberName)
                                                    .expirationDateAndTime(currentDateAndTime.get().plusMinutes(5)).build();

      int timesSubscribed = subscription.getTimesSubscribed() + 1;
      subscription.setTimesSubscribed(timesSubscribed);

      subscriptionsRepository.get().put(subscription);

    } else {

      Subscription subscription = subscriptionsRepository.get().get(eventType, subscriberName);
      subscription.setExpirationDateAndTime(currentDateAndTime.get().plusMinutes(5));

      int timesSubscribed = subscription.getTimesSubscribed() + 1;
      subscription.setTimesSubscribed(timesSubscribed);

      subscriptionsRepository.get().put(subscription);
    }
  }

  @Override
  public void unsubscribe(PushEvent.Type eventType) {

    String subscriberName = this.subscriber.get().getName();

    if (subscriptionsRepository.get().hasSubscription(eventType, subscriberName)) {

      Subscription subscription = subscriptionsRepository.get().get(eventType, subscriberName);

      int timesSubscribed = subscription.getTimesSubscribed() - 1;
      subscription.setTimesSubscribed(timesSubscribed);

      if (subscription.getTimesSubscribed() == 0) {
        subscriptionsRepository.get().removeSubscription(eventType, subscriberName);
      } else {
        subscription.setExpirationDateAndTime(currentDateAndTime.get().plusMinutes(5));
        subscriptionsRepository.get().put(subscription);
      }
    }
  }

  @Override
  public PushEvent dummyMethod() {
    return null;
  }
}
