package com.clouway.push.server;

import com.clouway.push.shared.PushEvent;
import com.google.appengine.api.channel.ChannelFailureException;
import com.google.appengine.api.channel.ChannelMessage;
import com.google.appengine.api.channel.ChannelService;
import com.google.appengine.api.channel.ChannelServiceFactory;
import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.server.rpc.RPC;
import com.google.gwt.user.server.rpc.SerializationPolicy;
import com.google.gwt.user.server.rpc.SerializationPolicyLoader;
import com.google.inject.Inject;
import com.google.inject.name.Named;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Ivan Lazov <ivan.lazov@clouway.com>
 */
public class PushServiceImpl implements PushService {

  private ActiveSubscriptionsFilter filter;
  private final String serializationPolicyDirectory;

  @Inject
  public PushServiceImpl(ActiveSubscriptionsFilter filter, @Named("SerializationPolicyDirectory") String serializationPolicyDirectory) {
    this.filter = filter;
    this.serializationPolicyDirectory = serializationPolicyDirectory;
  }

  public void pushEvent(PushEvent event) {

    String message = encodeMessage(event);

    List<Subscription> subscriptions = filter.filterSubscriptions(event.getAssociatedType());

    ChannelService channelService = ChannelServiceFactory.getChannelService();

    try {
      for (Subscription subscription : subscriptions) {
        channelService.sendMessage(new ChannelMessage(subscription.getSubscriber(), message));
      }
    } catch (ChannelFailureException exception) {
      throw new UnableToPushEventException(exception.getMessage());
    }
  }

  private String encodeMessage(PushEvent event) {
    try {
      return RPC.encodeResponseForSuccess(getDummyMethod(), event, getSerializationPolicy());
    } catch (SerializationException e) {
      throw new RuntimeException("Unable to encode a message for push.\n" + event.getAssociatedType(), e);
    }
  }

  private PushEvent dummyMethod() {
    throw new UnsupportedOperationException("This should never be called.");
  }

  private Method getDummyMethod() {
    try {
      return PushServiceImpl.class.getDeclaredMethod("dummyMethod");
    } catch (NoSuchMethodException e) {
      throw new RuntimeException("Unable to find the dummy RPC method.");
    }
  }

  private SerializationPolicy getSerializationPolicy() {
    // Read all of the SerializationPolicy files in the app and merging them together.

    File directory = new File(serializationPolicyDirectory);

    File[] files = directory.listFiles(new FilenameFilter() {

      public boolean accept(File dir, String name) {
        return name.endsWith(".gwt.rpc");
      }
    });

    List<SerializationPolicy> serializationPolicies = new ArrayList<SerializationPolicy>();

    for (File file : files) {
      try {
        BufferedInputStream input = new BufferedInputStream(new FileInputStream(file));
        serializationPolicies.add(SerializationPolicyLoader.loadFromStream(input, null));
      } catch (Exception e) {
        throw new RuntimeException("Unable to load a policy file: " + file.getAbsolutePath());
      }
    }

    return new MergedSerializationPolicy(serializationPolicies);
  }

  private class MergedSerializationPolicy extends SerializationPolicy {
    List<SerializationPolicy> serializationPolicies;

    MergedSerializationPolicy(List<SerializationPolicy> serializationPolicies) {
      this.serializationPolicies = serializationPolicies;
    }

    @Override
    public boolean shouldDeserializeFields(Class<?> clazz) {
      for (SerializationPolicy serializationPolicy : serializationPolicies) {
        if (serializationPolicy.shouldDeserializeFields(clazz)) {
          return true;
        }
      }
      return false;
    }

    @Override
    public boolean shouldSerializeFields(Class<?> clazz) {
      for (SerializationPolicy serializationPolicy : serializationPolicies) {
        if (serializationPolicy.shouldSerializeFields(clazz)) {
          return true;
        }
      }
      return false;
    }

    @Override
    public void validateDeserialize(Class<?> clazz) throws SerializationException {
      SerializationException serializationException = null;
      for (SerializationPolicy serializationPolicy : serializationPolicies) {
        try {
          serializationPolicy.validateDeserialize(clazz);
          return;
        } catch (SerializationException e) {
          serializationException = e;
        }
      }
      throw serializationException;
    }

    @Override
    public void validateSerialize(Class<?> clazz) throws SerializationException {
      SerializationException se = null;
      for (SerializationPolicy serializationPolicy : serializationPolicies) {
        try {
          serializationPolicy.validateSerialize(clazz);
          return;
        } catch (SerializationException e) {
          se = e;
        }
      }
      throw se;
    }
  }
}
