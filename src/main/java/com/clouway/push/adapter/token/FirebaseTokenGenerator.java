package com.clouway.push.adapter.token;

import com.clouway.push.core.AccessToken;
import com.clouway.push.core.TokenGenerator;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @author Stanislava Kaukova (stanislava.kaukova@clouway.com).
 */
public class FirebaseTokenGenerator implements TokenGenerator {
  private static final String IDENTITY_ENDPOINT =
          "https://identitytoolkit.googleapis.com/google.identity.identitytoolkit.v1.IdentityToolkit";

  private static final Collection FIREBASE_SCOPES = Arrays.asList(
          "https://www.googleapis.com/auth/firebase.database",
          "https://www.googleapis.com/auth/userinfo.email"
  );

  private final String firebaseServiceAccount;

  public FirebaseTokenGenerator(String firebaseServiceAccount) {
    this.firebaseServiceAccount = firebaseServiceAccount;
  }

  @Override
  public String generateCustomToken() {
    GoogleCredential googleCredential = getGoogleCredentials();
    long timeSeconds = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis());
    long expSeconds = timeSeconds + 60L * 60L;

    Map<String, Object> headers = new HashMap<>();
    headers.put("alg", "RS256");
    headers.put("typ", "JWT");

    if (googleCredential != null) {
      return Jwts.builder()
              .setHeaderParams(headers)
              .claim("uid", UUID.randomUUID().toString())
              .claim("iat", timeSeconds)
              .claim("exp", expSeconds)
              .claim("aud", IDENTITY_ENDPOINT)
              .claim("iss", googleCredential.getServiceAccountId())
              .claim("sub", googleCredential.getServiceAccountId())
              .signWith(SignatureAlgorithm.RS256, googleCredential.getServiceAccountPrivateKey())
              .compact();
    }

    return "";
  }

  @Override
  public AccessToken generateAccessToken() {
    GoogleCredential googleCredential = getGoogleCredentials();
    if (googleCredential != null) {
      GoogleCredential credentials = googleCredential.createScoped(FIREBASE_SCOPES);
      try {
        credentials.refreshToken();
      } catch (IOException e) {
        e.printStackTrace();
      }

      String accessToken = credentials.getAccessToken();

      return new AccessToken(accessToken, credentials.getExpiresInSeconds());
    }
    return new AccessToken("", null);
  }


  private GoogleCredential getGoogleCredentials() {
    InputStream configStream;

    try {
      configStream = new ByteArrayInputStream(firebaseServiceAccount.getBytes(StandardCharsets.UTF_8.name()));

      // Authenticate a Google credential with the service account
      return GoogleCredential.fromStream(configStream);
    } catch (IOException e) {
      e.printStackTrace();
    }

    return null;
  }
}
