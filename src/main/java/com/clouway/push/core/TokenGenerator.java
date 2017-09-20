package com.clouway.push.core;

/**
 * Will be used to generate tokens
 *
 * @author Stanislava Kaukova (stanislava.kaukova@clouway.com).
 */
public interface TokenGenerator {
  /**
   * Generate access token
   *
   * @return generated access token
   */
  AccessToken generateAccessToken();

  /**
   * Generate custom token
   *
   * @return generated custom token
   */

  String generateCustomToken();
}
