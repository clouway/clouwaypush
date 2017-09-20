package com.clouway.push.core;

/**
 * @author Stanislava Kaukova (stanislava.kaukova@clouway.com).
 */
public class ChannelFailureException extends RuntimeException{
  public ChannelFailureException(String message) {
    super(message);
  }
}
