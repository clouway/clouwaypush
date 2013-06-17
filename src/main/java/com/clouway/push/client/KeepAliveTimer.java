package com.clouway.push.client;

/**
 * @author Ivan Lazov <ivan.lazov@clouway.com>
 */
public interface KeepAliveTimer {

  void onTime(OnTimeCallBack callBack);

  int getSeconds();

  void scheduleAction(int seconds, TimerAction action);
}
