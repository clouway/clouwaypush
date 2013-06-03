package com.clouway.push.server;

import com.clouway.push.shared.PushEvent;
import com.clouway.push.client.channelapi.PushChannelService;
import com.google.appengine.api.channel.ChannelServiceFactory;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

/**
 * @author Ivan Lazov <ivan.lazov@clouway.com>
 */
@Singleton
public class PushChannelServiceImpl extends RemoteServiceServlet implements PushChannelService {

  private final Provider<SubscriptionsRepository> subscriptionsRepository;
  private final Provider<Subscriber> subscriber;

  @Inject
  public PushChannelServiceImpl(Provider<SubscriptionsRepository> subscriptionsRepository, Provider<Subscriber> subscriber) {
    this.subscriptionsRepository = subscriptionsRepository;
    this.subscriber = subscriber;
  }

  @Override
  public String open() {
    return ChannelServiceFactory.getChannelService().createChannel(subscriber.get().getName());
  }

  @Override
  public void subscribe(PushEvent.Type type) {
    subscriptionsRepository.get().subscribe(subscriber.get().getName(), type);
  }

  @Override
  public void unsubscribe(PushEvent event) {

  }

  @Override
  public PushEvent dummyMethod() {
    return null;
  }
}
