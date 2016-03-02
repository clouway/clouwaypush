package com.clouway.push.server;

import com.clouway.push.client.channelapi.PushChannelService;
import com.clouway.push.shared.util.DateTime;
import com.google.appengine.api.channel.ChannelService;
import com.google.appengine.api.channel.ChannelServiceFactory;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;
import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.Provider;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.google.inject.servlet.ServletModule;

/**
 * @author Ivan Lazov <ivan.lazov@clouway.com>
 */
public class PushChannelModule extends AbstractModule {

  private int subscriptionsExpirationMinutes;
  private final Class<? extends EventSerializer> encoder;

  public PushChannelModule(int subscriptionsExpirationMinutes, Class<? extends EventSerializer> encoder) {
    this.subscriptionsExpirationMinutes = subscriptionsExpirationMinutes;
    this.encoder = encoder;
  }

  @Override
  protected final void configure() {

    bind(PushService.class).to(PushServiceImpl.class).in(Singleton.class);
    bind(SubscriptionsRepository.class).to(MemcacheSubscriptionsRepository.class);

    install(new ServletModule() {
      @Override
      protected void configureServlets() {
        serve("/pushChannelService").with(PushChannelServiceImpl.class);
        serve("/pushService").with(PushChannelRestService.class);
      }
    });
  }

  @Provides
  @SubscriptionsExpirationDate
  DateTime getSubscriptionExpirationDate() {
    return new DateTime().plusMills(subscriptionsExpirationMinutes * 60 * 1000);
  }

  @Provides
  @SubscriptionsExpirationMills
  Integer getSubscriptionExpirationMills() {
    return subscriptionsExpirationMinutes * 60 * 1000;
  }

  @Provides
  @CurrentDate
  DateTime getCurrentDate() {
    return new DateTime();
  }

  @Provides
  @Singleton
  public EventSerializer getEncoder(Injector injector) {
    return injector.getInstance(encoder);
  }

  @Provides
  @Named("MemcacheService")
  MemcacheService getMemcacheService() {
    return MemcacheServiceFactory.getMemcacheService();
  }

  @Provides
  public ChannelService getChannelService() {
    return ChannelServiceFactory.getChannelService();
  }

  @Provides
  public PushChannelService getPushChannelService(Provider<SubscriptionsRepository> subscriptionsRepository,
                                                  @SubscriptionsExpirationDate Provider<DateTime> subscriptionsExpirationDate) {
    return new PushChannelServiceImpl(subscriptionsRepository, subscriptionsExpirationDate);
  }
}
