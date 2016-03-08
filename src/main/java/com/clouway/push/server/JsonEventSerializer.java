package com.clouway.push.server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * JsonEncoder is representing JSON codec which is used by the PUSH api for sending of JSON messages to the clients.
 * <p/>
 *
 * @author Miroslav Genov (miroslav.genov@clouway.com)
 */
class JsonEventSerializer implements EventSerializer {
  private static final Gson gson = new GsonBuilder().create();

  @Override
  public String serialize(PushEventSource eventSource) {
    String json = gson.toJson(eventSource);

    return json;
  }
}
