package com.clouway.push.server;

import com.clouway.push.shared.PushEvent;
import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.server.rpc.RPC;
import com.google.gwt.user.server.rpc.SerializationPolicy;
import com.google.gwt.user.server.rpc.SerializationPolicyLoader;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Used GWT RPC serialization policy to serialize given {@link PushEvent} to string.
 *
 * @author Georgi Georgiev (GeorgievJon@gmail.com)
 */
class RpcEncoder implements Encoder {

  private String serializationPolicyDirectory;

  private SerializationPolicy serializationPolicy;

  public RpcEncoder(String serializationPolicyDirectory) {
    this.serializationPolicyDirectory = serializationPolicyDirectory;
    serializationPolicy = getSerializationPolicy();
  }

  @Override
  public String encode(PushEvent event) {

    try {
      return RPC.encodeResponseForSuccess(getDummyMethod(), event, serializationPolicy);
    } catch (SerializationException e) {
      throw new RuntimeException("Unable to encode a message for push.\n" + event.getAssociatedType(), e);
    }
  }

  //this method is used from serialization SerializationPolicy
  @SuppressWarnings("unused")
  private PushEvent dummyMethod() {
    throw new UnsupportedOperationException("This should never be called.");
  }

  private Method getDummyMethod() {
    try {
      return RpcEncoder.class.getDeclaredMethod("dummyMethod");
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
}
