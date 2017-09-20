package com.clouway.push.core;

/**
 * @author Stanislava Kaukova (stanislava.kaukova@clouway.com).
 */
public interface Provider<T> {
  T get();
}
