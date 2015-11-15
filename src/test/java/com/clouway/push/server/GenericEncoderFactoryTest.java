package com.clouway.push.server;

import com.clouway.push.server.testevents.AddPersonEvent;
import com.clouway.push.server.testevents.GenericJsonEvent;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.junit.Rule;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.*;


/**
 * @author Miroslav Genov (miroslav.genov@clouway.com)
 */
public class GenericEncoderFactoryTest {

  @Rule
  public JUnitRuleMockery context = new JUnitRuleMockery();

  Encoder rpcEncoder = context.mock(Encoder.class,"rpcEncoder");
  Encoder jsonEncoder = context.mock(Encoder.class, "jsonEncoder");

  @Test
  public void rpcEncoderIsCreated() {
    GenericEncoderFactory factory = new GenericEncoderFactory(rpcEncoder, jsonEncoder);

    Encoder encoder = factory.create(new AddPersonEvent("John",20));
    assertThat(encoder,is(sameInstance(rpcEncoder)));
  }

  @Test
  public void jsonEncoderIsCreated() {
    GenericEncoderFactory factory = new GenericEncoderFactory(rpcEncoder, jsonEncoder);

    Encoder encoder = factory.create(new GenericJsonEvent());
    assertThat(encoder, is(sameInstance(jsonEncoder)));
  }

}