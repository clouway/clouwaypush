package com.clouway.push.client;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;

public class InstanceMatcher<T> extends BaseMatcher<T> {
    private T instance;

    public T getInstance() {
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
