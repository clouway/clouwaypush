package com.clouway.push.core;

import com.google.api.client.http.HttpResponse;

import java.io.IOException;

/**
 * @author Stanislava Kaukova (stanislava.kaukova@clouway.com).
 */
public interface HttpClient {
  HttpResponse sendMessage(String namespace, String key, String message) throws ChannelFailureException;
}
