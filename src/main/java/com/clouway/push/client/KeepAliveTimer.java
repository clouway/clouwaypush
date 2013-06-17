package com.clouway.push.client;

import java.util.List;

/**
 * @author Ivan Lazov <ivan.lazov@clouway.com>
 */
public interface KeepAliveTimer {

  void onTime(OnTimeCallBack callBack);

  int getSeconds();

  void scheduleTimedAction(int retriesCount, List<Integer> secondsDelays, TimedAction action);

  List<Integer> getSecondsDelays();
}
