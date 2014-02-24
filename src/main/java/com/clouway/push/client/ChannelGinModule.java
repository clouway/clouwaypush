package com.clouway.push.client;

import com.google.gwt.inject.client.AbstractGinModule;

/**
 * @author Ivan Lazov <ivan.lazov@clouway.com>
 */
public abstract class ChannelGinModule extends AbstractGinModule {

  @Override
  protected void configure() {

    configureKeepAliveTimeInterval();

    install(new PushChannelGinModule());
  }

  protected abstract void configureKeepAliveTimeInterval();

  protected final void setKeepAliveTimeInterval(int timeIntervalInSeconds) {
    bindConstant().annotatedWith(KeepAliveTimeInterval.class).to(timeIntervalInSeconds);
  }
}
