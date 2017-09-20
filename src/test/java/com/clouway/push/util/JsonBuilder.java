package com.clouway.push.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * @author Stanislava Kaukova (stanislava.kaukova@clouway.com)
 */
public class JsonBuilder {
  private static final Gson GSON = new GsonBuilder().create();

  public static JsonBuilder aNewJson() {
    return new JsonBuilder(new JsonObject());
  }

  private JsonElement target;

  public JsonBuilder(JsonElement target) {
    this.target = target;
  }

  public JsonBuilder add(String property, String value) {
    target.getAsJsonObject().addProperty(property, value);
    return this;
  }

  public String build() {
    return GSON.toJson(target);
  }

}
