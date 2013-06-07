package com.clouway.push.client;

import com.google.gwt.user.client.Timer;

/**
 * @author Ivan Lazov <ivan.lazov@clouway.com>
 */
public class ImAliveTimerImpl implements ImAliveTimer {

  private Timer timer;
  private int seconds;

  public ImAliveTimerImpl(int seconds) {
    this.seconds = seconds;
  }

  public void onTime(final OnTimeCallBack callBack){
    timer = new Timer() {
      @Override
      public void run() {
         callBack.onTime();
      }
    };
    timer.scheduleRepeating(seconds *1000);
  }

  public int getSeconds() {
    return seconds;
  }

  @Override
  public void schedule() {
    //timer.schedule(seconds *1000);
  }
}
