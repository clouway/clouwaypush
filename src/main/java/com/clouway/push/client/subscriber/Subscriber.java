package com.clouway.push.client.subscriber;

/**
 * @author Ivan Lazov <ivan.lazov@clouway.com>
 */
public class Subscriber {

  private static String value;

  public static String getValue() {
    return value;
  }

  public static void setValue(String value) {
    Subscriber.value = value;
  }
}
