package com.clouway.push.server;

import com.clouway.push.shared.PushEvent;
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

    String message = encoder.encode(event);

    List<Subscription> subscriptions = filter.filterSubscriptions(event.getAssociatedType());

    ChannelService channelService = channelServiceProvider.get();

    long start = System.currentTimeMillis();
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
