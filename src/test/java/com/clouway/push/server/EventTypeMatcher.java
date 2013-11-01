package com.clouway.push.server;

import com.clouway.push.shared.PushEvent;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

/**
 * @author Krasimir Dimitrov (krasimir.dimitrov@clouway.com, kpackapgo@gmail.com)
 */
public class EventTypeMatcher extends TypeSafeMatcher<PushEvent.Type> {
  @Override
  protected boolean matchesSafely(PushEvent.Type item) {
    return false;
  }

  @Override
  public void describeTo(Description description) {

  }

  public static EventTypeMatcher isType(final PushEvent.Type actualType){
    return new EventTypeMatcher(){
      @Override
      public boolean matchesSafely(PushEvent.Type expectedType) {
        return expectedType.equals(actualType);
      }
    };
  }

}
