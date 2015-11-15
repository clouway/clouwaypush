package com.clouway.push.server;

import com.clouway.push.shared.PushEvent;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * JsonEncoder is representing JSON codec which is used by the PUSH api for sending of JSON messages to the clients.
 * <p/>
 *
 * @author Miroslav Genov (miroslav.genov@clouway.com)
 */
class JsonEncoder implements Encoder {
  private static final Gson gson = new GsonBuilder().create();

  @Override
  public String encode(PushEvent event) {
    JsonElement element = gson.toJsonTree(event);
    JsonObject jsonObject = element.getAsJsonObject();
    jsonObject.addProperty("event", event.getAssociatedType().getKey());

    // If event object has not defined TYPE property as static it will be serialised
    // and this is removing this information from the content
    if (jsonObject.has("TYPE")) {
      jsonObject.remove("TYPE");
    }

    return element.toString();
  }
}
