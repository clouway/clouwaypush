package com.clouway.push.client;

import com.google.gwt.user.client.Timer;

import java.util.List;

/**
 * @author Ivan Lazov <ivan.lazov@clouway.com>
 */
public class KeepAliveTimerImpl implements KeepAliveTimer {

  private Timer timer;

  private final int seconds;
  private final List<Integer> secondsDelays;

  public KeepAliveTimerImpl(int seconds, List<Integer> secondsDelays) {
    this.seconds = seconds;
    this.secondsDelays = secondsDelays;
  }

  public void onTime(final OnTimeCallBack callback){
    timer = new Timer() {
      @Override
      public void run() {
        callback.onTime();
      }
    };
    timer.scheduleRepeating(seconds * 1000);
  }

  @Override
  public void scheduleTimedAction(int retriesCount, List<Integer> secondsDelays, final TimedAction action) {

    timer = new Timer() {
      @Override
      public void run() {
        action.execute();
      }
    };
    timer.schedule(secondsDelays.get(retriesCount));
  }

  @Override
  public List<Integer> getSecondsDelays() {
    return secondsDelays;
  }

  public int getSeconds() {
    return seconds;
  }
}
