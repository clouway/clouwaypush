package com.clouway.push.client;

import com.clouway.push.client.channelapi.Channel;
import com.clouway.push.client.channelapi.ChannelImpl;
import com.clouway.push.client.channelapi.PushChannelService;
import com.clouway.push.client.channelapi.PushChannelServiceAsync;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.SimpleEventBus;
import com.google.gwt.inject.client.AbstractGinModule;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.inject.Inject;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Names;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * @author Ivan Lazov <ivan.lazov@clouway.com>
 */
class PushChannelGinModule extends AbstractGinModule {

  @Override
  protected void configure() {

    bind(PushChannelApi.class).to(PushChannelApiImpl.class).in(Singleton.class);
    bind(PushEventBus.class).to(ChannelApiPushEventBus.class).in(Singleton.class);
    bind(EventBus.class).annotatedWith(Names.named("PushEventBus")).to(SimpleEventBus.class).in(Singleton.class);
    bind(Channel.class).to(ChannelImpl.class).in(Singleton.class);
  }

  @Singleton
  @Provides
  PushChannelServiceAsync getPushChannelServiceAsync() {

    PushChannelServiceAsync pushChannelServiceAsync = GWT.create(PushChannelService.class);
    ((ServiceDefTarget) pushChannelServiceAsync).setServiceEntryPoint("/pushChannelService");

    return pushChannelServiceAsync;
  }

  @Provides
  @Singleton
  @Inject
  public KeepAliveTimer getImAliveTimer(@KeepAliveTimeInterval int timeIntervalInSeconds, @ChanelReconnectTimeInterval int reconnectTimeIntervalInSecond) {
    return new KeepAliveTimerImpl(timeIntervalInSeconds, reconnectTimeIntervalInSecond);
  }

  @Provides
  @CurrentSubscriber
  @Singleton
  public String getCurrentSubscriber() {
    return getSubscriberToken(15);
  }

  @Provides
  @SubscribeRequestSecondsRetries
  @Singleton
  public List<Integer> getSubscribeRequestSecondsRetries() {

    List<Integer> secondsRetries = new ArrayList<Integer>();
    secondsRetries.add(1);

    return secondsRetries;
  }

  @Provides
  @UnsubscribeRequestSecondsRetries
  @Singleton
  public List<Integer> getUnsubscribeRequestSecondsRetries() {

    List<Integer> secondsRetries = new ArrayList<Integer>();
    secondsRetries.add(1);

    return secondsRetries;
  }

  @Provides
  @KeepAliveRequestSecondsRetries
  @Singleton
  public List<Integer> getKeepAliveRequestSecondsRetries() {

    List<Integer> secondsRetries = new ArrayList<Integer>();
    secondsRetries.add(1);
    secondsRetries.add(2);
    secondsRetries.add(3);
    secondsRetries.add(5);
    secondsRetries.add(8);
    secondsRetries.add(12);

    return secondsRetries;
  }

  private String getSubscriberToken(int length) {

    String characters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ123456";

    Random random = new Random();
    StringBuilder sb = new StringBuilder();

    for (int i = 0; i < length; i++) {
      int position = random.nextInt(characters.length());
      sb.append(characters.charAt(position));
    }

    return sb.toString();
  }
}
