package com.clouway.push.client;

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

    bind(PushChannel.class).to(PushChannelImpl.class).in(Singleton.class);

    bind(PushEventBus.class).to(SimplePushEventBus.class).in(Singleton.class);
  }

  @Singleton
  @Provides
  PushChannelServiceAsync getPushChannelServiceAsync() {

    PushChannelServiceAsync pushChannelServiceAsync = GWT.create(PushChannelService.class);
    ((ServiceDefTarget) pushChannelServiceAsync).setServiceEntryPoint("/pushChannelService");

    return pushChannelServiceAsync;
  }
}
