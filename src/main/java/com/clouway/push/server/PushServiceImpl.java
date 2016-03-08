package com.clouway.push.server;

import com.google.appengine.api.channel.ChannelFailureException;
import com.google.appengine.api.channel.ChannelMessage;
import com.google.appengine.api.channel.ChannelService;
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
  private EventSerializer eventSerializer;
  private final Provider<ChannelService> channelServiceProvider;

  @Inject
  public PushServiceImpl(SubscriptionsRepository subscriptions, EventSerializer eventSerializer, Provider<ChannelService> channelServiceProvider) {
    this.subscriptions = subscriptions;
    this.eventSerializer = eventSerializer;
    this.channelServiceProvider = channelServiceProvider;
  }

  public void pushEvent(PushEvent event) {
    pushEvent(event, "");
  }

  @Override
  public void pushEvent(PushEvent event, String correlationId) {

    String key = event.getKey() + correlationId;

    String message = eventSerializer.serialize(new PushEventSource(event, correlationId));

    long start = System.currentTimeMillis();
    List<Subscription> subscriptions = this.subscriptions.findSubscriptions(key);
    log.info("Find subscriptions: " + subscriptions.size() + " for " + key + " " + (System.currentTimeMillis() - start) + " ms");

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
