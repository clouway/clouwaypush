package com.clouway.push.core;

import java.io.Serializable;

/**
 * @author Stanislava Kaukova <stanislava.kaukova@clouway.com>
 */
public final class AccessToken implements Serializable {
  public final String value;
  private final Long expiresAt;

  public AccessToken(String value, Long expiresAt) {
    this.value = value;
    this.expiresAt = expiresAt;
  }
  public Long expirationTimestamp() {
    return expiresAt;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    AccessToken that = (AccessToken) o;

    if (value != null ? !value.equals(that.value) : that.value != null) return false;
    return expiresAt != null ? expiresAt.equals(that.expiresAt) : that.expiresAt == null;
  }

  @Override
  public int hashCode() {
    int result = value != null ? value.hashCode() : 0;
    result = 31 * result + (expiresAt != null ? expiresAt.hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return "AccessToken{" +
            "value='" + value + '\'' +
            ", expiresAt=" + expiresAt +
            '}';
  }
}