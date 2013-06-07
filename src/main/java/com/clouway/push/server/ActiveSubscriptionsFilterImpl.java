package com.clouway.push.server;

import com.clouway.push.shared.PushEvent;
import com.clouway.push.shared.util.DateTime;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Provider;

import java.util.List;

/**
 * @author Ivan Lazov <ivan.lazov@clouway.com>
 */
public class ActiveSubscriptionsFilterImpl implements ActiveSubscriptionsFilter {

  private SubscriptionsRepository subscriptionsRepository;
  private Provider<DateTime> currentDate;

  @Inject
  public ActiveSubscriptionsFilterImpl(SubscriptionsRepository subscriptionsRepository, @CurrentDateAndTime Provider<DateTime>  currentDate) {
    this.subscriptionsRepository = subscriptionsRepository;
    this.currentDate = currentDate;
  }

  @Override
  public List<Subscription> filterSubscriptions(PushEvent.Type type) {
    List<Subscription> subscriptions = subscriptionsRepository.findSubscriptions(type);

    List<Subscription> activeSubscriptions = Lists.newArrayList();

    for (Subscription subscription : subscriptions) {
      if(subscription.isActive(currentDate.get())){
        activeSubscriptions.add(subscription);
      }else {
        subscriptionsRepository.removeSubscription(subscription);
      }
    }

    return activeSubscriptions;
  }
}
