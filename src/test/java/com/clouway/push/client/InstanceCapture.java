package com.clouway.push.client;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;

public class InstanceCapture<T> extends BaseMatcher<T> {

  private T instance;

  public T getValue() {
    return instance;
  }

  public boolean matches(Object o) {
    try {
      instance = (T) o;
      return true;
    } catch (ClassCastException ex) {
      return false;
    }
  }

  @Override
  public void describeTo(Description description) {
  }
}
