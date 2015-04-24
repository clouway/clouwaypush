package com.clouway.push.server;

import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.server.rpc.SerializationPolicy;

import java.util.List;

/**
 * @author Georgi Georgiev (GeorgievJon@gmail.com)
 */
public class MergedSerializationPolicy extends SerializationPolicy {
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
