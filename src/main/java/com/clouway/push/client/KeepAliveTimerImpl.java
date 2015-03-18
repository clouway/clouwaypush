package com.clouway.push.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Timer;

/**
 * @author Ivan Lazov <ivan.lazov@clouway.com>
 */
public class KeepAliveTimerImpl implements KeepAliveTimer {

  private final int keepAliveSeconds;
  private final int reconnectSeconds;

  public KeepAliveTimerImpl(int keepAliveSeconds, int reconnectSeconds) {
    this.keepAliveSeconds = keepAliveSeconds;
    this.reconnectSeconds = reconnectSeconds;
  }

  public void onTime(final OnTimeCallBack callback){
    Timer timer = new Timer() {
      @Override
      public void run() {
        callback.onTime();
      }
    };
    timer.scheduleRepeating(keepAliveSeconds * 1000);
  }

  @Override
  public void reconnect(final ChanelReconnectScheduler scheduler) {
    Timer timer = new Timer() {
      @Override
      public void run() {
        scheduler.reconnect();
        GWT.log("try to reconnect");
      }
    };
    timer.schedule(reconnectSeconds * 1000);
  }

  @Override
  public void scheduleAction(int seconds, final TimerAction action) {

    Timer timer = new Timer() {
      @Override
      public void run() {
        action.execute();
      }
    };
    timer.schedule(seconds * 1000);
  }
}
