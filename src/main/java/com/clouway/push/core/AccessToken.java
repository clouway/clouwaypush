package com.clouway.push.core;

import java.io.Serializable;

/**
 * @author Stanislava Kaukova <stanislava.kaukova@clouway.com>
 */
public final class AccessToken implements Serializable {
  public final String value;
  private final Long expiresInSeconds;

  public AccessToken(String value, Long expiresInSeconds) {
    this.value = value;
    this.expiresInSeconds = expiresInSeconds;
  }

  public Long getExpiresInSeconds() {
    return expiresInSeconds;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    AccessToken that = (AccessToken) o;

    if (!value.equals(that.value)) return false;
    return expiresInSeconds.equals(that.expiresInSeconds);
  }

  @Override
  public int hashCode() {
    int result = value.hashCode();
    result = 31 * result + expiresInSeconds.hashCode();
    return result;
  }
}