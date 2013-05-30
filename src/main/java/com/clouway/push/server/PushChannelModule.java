package com.clouway.push.server;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import com.google.inject.name.Names;
import com.google.inject.servlet.ServletModule;

/**
 * @author Ivan Lazov <ivan.lazov@clouway.com>
 */
public class PushChannelModule extends AbstractModule {

  private final String serializationPolicyDirectory;

  public PushChannelModule(String serializationPolicyDirectory) {
    this.serializationPolicyDirectory = serializationPolicyDirectory;
  }

  @Override
  protected final void configure() {

    bind(PushService.class).to(PushServiceImpl.class).in(Singleton.class);
    bind(String.class).annotatedWith(Names.named("SerializationPolicyDirectory")).toInstance(serializationPolicyDirectory);

    install(new ServletModule(){
      @Override
      protected void configureServlets() {
        serve("/pushChannelService").with(PushChannelServiceImpl.class);
      }
    });
  }


}
