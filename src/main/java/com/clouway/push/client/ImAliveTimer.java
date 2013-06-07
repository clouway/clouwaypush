package com.clouway.push.client;

/**
 * @author Ivan Lazov <ivan.lazov@clouway.com>
 */
public interface ImAliveTimer {

  void onTime(OnTimeCallBack callBack);

  int getSeconds();

  void schedule();
}
