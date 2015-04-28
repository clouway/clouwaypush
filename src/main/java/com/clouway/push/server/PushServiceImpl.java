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
public class PushServiceImpl implements PushService {

  private static final Logger log = Logger.getLogger(PushServiceImpl.class.getName());

  private ActiveSubscriptionsFilter filter;
  private Encoder encoder;
  private Provider<ChannelService> channelServiceProvider;

  @Inject
  public PushServiceImpl(ActiveSubscriptionsFilter filter, Encoder encoder, Provider<ChannelService> channelServiceProvider) {
    this.filter = filter;
    this.encoder = encoder;
    this.channelServiceProvider = channelServiceProvider;
  }

  public void pushEvent(PushEvent event) {
    pushEvent(event, "");
  }

  @Override
  public void pushEvent(PushEvent event, String correlationId) {
    String message = encoder.encode(event);

    // transforming the eventType
    if (!Strings.isNullOrEmpty(correlationId)) {
      event.getAssociatedType().setCorrelationId(correlationId);
    }

    long start = System.currentTimeMillis();
    List<Subscription> subscriptions = filter.filterSubscriptions(event.getAssociatedType());
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
