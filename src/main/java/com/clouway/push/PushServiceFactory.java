package com.clouway.push;

import com.clouway.push.adapter.firebase.HttpFirebaseClient;
import com.clouway.push.adapter.token.IdGeneratorImpl;
import com.clouway.push.core.EventSerializer;
import com.clouway.push.core.Provider;
import com.clouway.push.core.TokenGenerator;
import com.clouway.push.core.Tokens;
import com.clouway.push.server.PushService;
import com.google.api.client.extensions.appengine.http.UrlFetchTransport;

/**
 * @author Stanislava Kaukova (stanislava.kaukova@clouway.com).
 */
public class PushServiceFactory {
  public static PushService create(EventSerializer eventSerializer, String firebaseDbUrl, Provider namespaceProvider, Tokens tokens, TokenGenerator tokenGenerator) {
    return new PushServiceImpl(eventSerializer, new HttpFirebaseClient(new UrlFetchTransport(), tokens, firebaseDbUrl, tokenGenerator), namespaceProvider, new IdGeneratorImpl());
  }
}
