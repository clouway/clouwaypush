package com.clouway.push.adapter.firebase;

import com.clouway.push.core.AccessToken;
import com.clouway.push.core.ChannelFailureException;
import com.clouway.push.core.HttpClient;
import com.clouway.push.core.TokenGenerator;
import com.clouway.push.core.Tokens;
import com.google.api.client.http.HttpMethods;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.testing.http.MockHttpTransport;
import com.google.api.client.testing.http.MockLowLevelHttpRequest;
import com.google.api.client.testing.http.MockLowLevelHttpResponse;
import com.google.appengine.tools.development.testing.LocalAppIdentityServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.common.collect.Sets;
import com.google.common.io.ByteStreams;
import org.jmock.Expectations;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static com.clouway.push.util.JsonBuilder.aNewJson;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;

/**
 * @author Stanislava Kaukova (stanislava.kaukova@clouway.com).
 */
public class HttpFirebaseClientTest {
  @Rule
  public final JUnitRuleMockery context = new JUnitRuleMockery();

  private Tokens tokens = context.mock(Tokens.class);
  private TokenGenerator tokenGenerator = context.mock(TokenGenerator.class);

  private final LocalServiceTestHelper helper =
          new LocalServiceTestHelper(new LocalAppIdentityServiceTestConfig());

  private final String firebaseDbUrl = "https://clouwaytestapp.firebaseio.com/";

  @Before
  public void setUp() {
    helper.setUp();
  }

  @After
  public void tearDown() {
    helper.tearDown();
  }

  @Test
  public void sendFirebaseMessage() throws Exception {
    MockHttpTransport httpTransport = aNewHttpTransport(HttpMethods.PUT, new MockLowLevelHttpResponse()
            .setStatusCode(200)
            .setContentType("application/json")
            .setContent(aNewJson()
                    .add("correlationId", "")
                    .add("eventName", "::SomeEvent::")
                    .add("message", "::message::")
                    .build()));

    HttpClient firebaseChannel = new HttpFirebaseClient(httpTransport, tokens, firebaseDbUrl, tokenGenerator);

    context.checking(new Expectations() {{
      oneOf(tokens).getAccessToken();
      will(returnValue(new AccessToken("accessToken", 1234567L)));
    }});

    HttpResponse response = firebaseChannel.sendMessage("namespace", "eventName", "message");

    String saved = new String(ByteStreams.toByteArray(response.getContent()));

    assertThat(response.getStatusCode(), is(200));
    assertThat(saved, is(aNewJson().add("correlationId", "").add("eventName", "::SomeEvent::").add("message", "::message::").build()));
  }

  @Test
  public void sendFirebaseMessageGeneratingNewAccessToken() throws Exception {
    MockHttpTransport httpTransport = aNewHttpTransport(HttpMethods.PUT, new MockLowLevelHttpResponse()
            .setStatusCode(200)
            .setContentType("application/json")
            .setContent(aNewJson()
                    .add("correlationId", "")
                    .add("eventName", "::SomeEvent::")
                    .add("message", "::message::")
                    .build()));

    HttpClient firebaseChannel = new HttpFirebaseClient(httpTransport, tokens, firebaseDbUrl, tokenGenerator);

    context.checking(new Expectations() {{
      oneOf(tokens).getAccessToken();
      will(returnValue(null));

      oneOf(tokenGenerator).generateAccessToken();
      will(returnValue(new AccessToken("newAccessToken", 45676890L)));

      oneOf(tokens).save(new AccessToken("newAccessToken", 45676890L));
    }});

    HttpResponse response = firebaseChannel.sendMessage("namespace", "eventName", "message");

    String saved = new String(ByteStreams.toByteArray(response.getContent()));

    assertThat(response.getStatusCode(), is(200));
    assertThat(saved, is(aNewJson().add("correlationId", "").add("eventName", "::SomeEvent::").add("message", "::message::").build()));
  }

  @Test
  public void canNotBeSendWithInternalServerError() throws Exception {
    MockHttpTransport httpTransport = aNewHttpTransport(HttpMethods.PUT, new MockLowLevelHttpResponse()
            .setStatusCode(200)
            .setContentType("application/json")
            .setContent(aNewJson()
                    .add("correlationId", "::::")
                    .add("eventName", "::SomeEvent::")
                    .add("message", "::message::")
                    .build()));

    HttpClient firebaseChannel = new HttpFirebaseClient(httpTransport, tokens, firebaseDbUrl, tokenGenerator);

    context.checking(new Expectations() {{
      oneOf(tokens).getAccessToken();
      will(returnValue(new AccessToken("accessToken", 1234567L)));
    }});

    try {
      firebaseChannel.sendMessage("namespace", "eventName", "message");
    } catch (ChannelFailureException e) {
      assertThat(e.getMessage(), is("Error code: 500 was received while updating Firebase"));
    }
  }

  private MockHttpTransport aNewHttpTransport(String expectedMethod, MockLowLevelHttpResponse response) {
    return new MockHttpTransport.Builder()
            .setSupportedMethods(Sets.newHashSet(expectedMethod))
            .setLowLevelHttpRequest(new MockLowLevelHttpRequest().setResponse(response))
            .build();
  }
}