package com.clouway.push.adapter.token;

import com.clouway.push.core.IdGenerator;

import java.util.UUID;

/**
 * @author Stanislava Kaukova (stanislava.kaukova@clouway.com).
 */
public class IdGeneratorImpl implements IdGenerator {
  @Override
  public String generate() {
    return UUID.randomUUID().toString();
  }
}
