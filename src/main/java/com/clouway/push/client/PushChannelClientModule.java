package com.clouway.push.client;

import com.google.gwt.inject.client.AbstractGinModule;

/**
 * @author Ivan Lazov <ivan.lazov@clouway.com>
 */
public abstract class PushChannelClientModule extends AbstractGinModule {

  @Override
  protected void configure() {

    bindConstant().annotatedWith(KeepAliveTimeInterval.class).to(getKeepAliveTimeInterval());
    bindConstant().annotatedWith(ChanelReconnectTimeInterval.class).to(getReconnectTimeInterval());
    install(new PushChannelGinModule());
  }

  public abstract int getKeepAliveTimeInterval();

  public abstract int getReconnectTimeInterval();
}
