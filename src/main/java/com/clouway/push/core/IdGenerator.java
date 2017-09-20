package com.clouway.push.core;

/**
 * Will be used to generate unique id
 *
 * @author Stanislava Kaukova (stanislava.kaukova@clouway.com).
 */
public interface IdGenerator {
  /**
   * Will generate unique id
   *
   * @return generated id
   */
  String generate();
}
