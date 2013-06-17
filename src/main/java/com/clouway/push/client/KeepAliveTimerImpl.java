package com.clouway.push.client;

import com.google.gwt.user.client.Timer;

/**
 * @author Ivan Lazov <ivan.lazov@clouway.com>
 */
public class KeepAliveTimerImpl implements KeepAliveTimer {

  private Timer timer;

  private final int seconds;

  public KeepAliveTimerImpl(int seconds) {
    this.seconds = seconds;
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
  public void scheduleAction(int seconds, final TimerAction action) {

    timer = new Timer() {
      @Override
      public void run() {
        action.execute();
      }
    };
    timer.schedule(seconds * 1000);
  }

  public int getSeconds() {
    return seconds;
  }
}
