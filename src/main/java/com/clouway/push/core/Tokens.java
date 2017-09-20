package com.clouway.push.core;

/**
 * @author Stanislava Kaukova (stanislava.kaukova@clouway.com).
 */
public interface Tokens {
  AccessToken getAccessToken();

  void save(AccessToken accessToken);
}