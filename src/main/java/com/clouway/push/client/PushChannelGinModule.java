package com.clouway.push.client;

import com.clouway.push.client.channelapi.PushChannelService;
import com.clouway.push.client.channelapi.PushChannelServiceAsync;
import com.google.gwt.core.client.GWT;
import com.google.gwt.inject.client.AbstractGinModule;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.inject.Provides;
import com.google.inject.Singleton;

/**
 * @author Ivan Lazov <ivan.lazov@clouway.com>
 */
public class PushChannelGinModule extends AbstractGinModule {

  @Override
  protected void configure() {

    bind(PushChannelApi.class).to(PushChannelApiImpl.class).in(Singleton.class);
    bind(PushEventBus.class).to(ChannelApiPushEventBus.class).in(Singleton.class);
  }

  @Singleton
  @Provides
  PushChannelServiceAsync getPushChannelServiceAsync() {

    PushChannelServiceAsync pushChannelServiceAsync = GWT.create(PushChannelService.class);
    ((ServiceDefTarget) pushChannelServiceAsync).setServiceEntryPoint("/pushChannelService");

    return pushChannelServiceAsync;
  }
}
