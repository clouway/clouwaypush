package com.clouway.push.server;

import com.clouway.push.shared.PushEvent;
import com.google.appengine.api.channel.ChannelFailureException;
import com.google.appengine.api.channel.ChannelMessage;
import com.google.appengine.api.channel.ChannelService;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.Provider;

import java.util.List;
import java.util.logging.Logger;

/**
 * @author Ivan Lazov <ivan.lazov@clouway.com>
 */
class PushServiceImpl implements PushService {

  private static final Logger log = Logger.getLogger(PushServiceImpl.class.getName());

  private final SubscriptionsRepository subscriptions;
  private Encoder encoder;
  private final Provider<ChannelService> channelServiceProvider;

  @Inject
  public PushServiceImpl(SubscriptionsRepository subscriptions,  Encoder encoder, Provider<ChannelService> channelServiceProvider) {
    this.subscriptions = subscriptions;
    this.encoder = encoder;
    this.channelServiceProvider = channelServiceProvider;
  }

  public void pushEvent(PushEvent event) {
    pushEvent(event, "");
  }

  @Override
  public void pushEvent(PushEvent event, String correlationId) {

    // transforming the eventType
    if (!Strings.isNullOrEmpty(correlationId)) {
      event.getAssociatedType().setCorrelationId(correlationId);
    }

    String message = encoder.encode(event);

    long start = System.currentTimeMillis();
    List<Subscription> subscriptions = this.subscriptions.findSubscriptions(event.getAssociatedType());
    log.info("Find subscriptions: " + subscriptions.size() + " for " + event.getAssociatedType().getKey() + " " + (System.currentTimeMillis() - start) + " ms");

    ChannelService channelService = channelServiceProvider.get();

    start = System.currentTimeMillis();
    try {
      for (Subscription subscription : subscriptions) {
        channelService.sendMessage(new ChannelMessage(subscription.getSubscriber(), message));
      }
    } catch (ChannelFailureException exception) {
      throw new UnableToPushEventException(exception.getMessage());
    }

    log.info("Send all messages for: " + (System.currentTimeMillis() - start) + " ms");
  }

}
