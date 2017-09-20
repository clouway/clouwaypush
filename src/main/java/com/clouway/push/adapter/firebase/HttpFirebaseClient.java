package com.clouway.push.adapter.firebase;

import com.clouway.push.core.AccessToken;
import com.clouway.push.core.ChannelFailureException;
import com.clouway.push.core.HttpClient;
import com.clouway.push.core.TokenGenerator;
import com.clouway.push.core.Tokens;
import com.google.api.client.http.ByteArrayContent;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpResponseException;
import com.google.api.client.http.HttpTransport;

import java.io.IOException;

public class HttpFirebaseClient implements HttpClient {
  private final String firebaseDbUrl;
  private final HttpTransport httpTransport;
  private final Tokens tokens;
  private final TokenGenerator tokenGenerator;

  public HttpFirebaseClient(HttpTransport httpTransport, Tokens tokens, String firebaseDbUrl, TokenGenerator tokenGenerator) {
    this.httpTransport = httpTransport;
    this.tokens = tokens;
    this.firebaseDbUrl = firebaseDbUrl;
    this.tokenGenerator = tokenGenerator;
  }

  public HttpResponse sendMessage(String namespace, String eventKey, String eventMessage) {
    HttpRequestFactory requestFactory = httpTransport.createRequestFactory();
    AccessToken accessToken = tokens.getAccessToken();

    if (accessToken == null) {
      accessToken = tokenGenerator.generateAccessToken();
      tokens.save(accessToken);
    }

    GenericUrl url = new GenericUrl(
            String.format("%s%s/%s/%s.json?access_token=" + accessToken.value, firebaseDbUrl, namespace, eventKey, "message"));
    try {

      return requestFactory.buildPutRequest(
              url, new ByteArrayContent("application/json", eventMessage.getBytes("UTF-8"))).execute();

    } catch (HttpResponseException e) {
      throw new ChannelFailureException("Error code: " + e.getStatusCode() + " was received while updating Firebase");
    } catch (IOException e) {
      throw new ChannelFailureException("Error was received while updating Firebase" + e.getMessage());
    }
  }
}
