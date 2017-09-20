package com.clouway.push.adapter.json;

import com.clouway.push.core.PushEventSource;
import com.clouway.push.testevents.AddPersonEvent;
import com.clouway.push.testevents.RemovePersonEvent;
import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;


/**
 * @author Miroslav Genov (miroslav.genov@clouway.com)
 */
public class JsonEventSerializerTest {
  JsonEventSerializer encoder = new JsonEventSerializer();

  @Test
  public void happyPath() {
    String json = encoder.serialize(new PushEventSource(new AddPersonEvent("John", 12), "", "123"));
    assertThat(json, is(equalTo("{\"event\":{\"name\":\"John\",\"age\":12,\"key\":\"addPersonEvent\"},\"correlationId\":\"\",\"id\":\"123\"}")));
  }

  @Test
  public void anotherEvent() {
    String json = encoder.serialize(new PushEventSource(new RemovePersonEvent(12), "", "235"));
    assertThat(json, is(equalTo("{\"event\":{\"personId\":12,\"key\":\"removePersonEvent\"},\"correlationId\":\"\",\"id\":\"235\"}")));
  }

  @Test
  public void eventWithCorrelationId() {
    String json = encoder.serialize(new PushEventSource(new RemovePersonEvent(12), "", "456"));
    assertThat(json, is(equalTo("{\"event\":{\"personId\":12,\"key\":\"removePersonEvent\"},\"correlationId\":\"\",\"id\":\"456\"}")));
  }

}