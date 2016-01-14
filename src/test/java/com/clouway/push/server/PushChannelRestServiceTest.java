package com.clouway.push.server;

import com.clouway.push.client.channelapi.PushChannelService;
import com.clouway.push.shared.PushEvent;
import com.google.common.collect.Lists;
import org.jmock.Expectations;
import org.jmock.auto.Mock;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * @author Stefan Dimitrov (stefan.dimitrov@clouway.com)
 */
public class PushChannelRestServiceTest {

  @Rule
  public JUnitRuleMockery context = new JUnitRuleMockery();

  @Mock
  private PushChannelService pushChannelService;
  @Mock
  private HttpServletRequest request;
  @Mock
  private HttpServletResponse response;

  private PushChannelRestService restService;

  @Before
  public void setUp() throws Exception {
    restService = new PushChannelRestService(pushChannelService);
  }

  @Test
  public void getChannelConnection() throws Exception {
    final String subscriber = "test-subscriber";

    final ServletOutputStreamStub outputStream = new ServletOutputStreamStub();

    context.checking(new Expectations() {{
      allowing(request).getParameter("subscriber");
      will(returnValue(subscriber));

      oneOf(pushChannelService).connect(subscriber);
      will(returnValue("channel-token"));

      allowing(response).getOutputStream();
      will(returnValue(outputStream));
    }});

    restService.doGet(request, response);

    assertThat(outputStream.getOutput(), is("channel-token"));
  }

  @Test
  public void subscribeForEvent() throws Exception {
    final String subscriber = "test-subscriber";
    final String eventName = "event";

    context.checking(new Expectations() {{
      allowing(request).getParameter("subscriber");
      will(returnValue(subscriber));
      allowing(request).getParameterValues("eventName");
      will(returnValue(new String[]{eventName}));

      oneOf(pushChannelService).subscribe(subscriber, Lists.newArrayList(new PushEvent.Type(eventName)));
    }});

    restService.doPut(request, response);
  }

  @Test
  public void subscribeForManyEvents() throws Exception {
    final String subscriber = "test-subscriber";
    final String[] eventNames = {"event1", "event2", "event3"};
    final List<PushEvent.Type> eventTypes = Lists.newArrayList(
            new PushEvent.Type("event1"),
            new PushEvent.Type("event2"),
            new PushEvent.Type("event3")
    );

    context.checking(new Expectations() {{
      allowing(request).getParameter("subscriber");
      will(returnValue(subscriber));
      allowing(request).getParameterValues("eventName");
      will(returnValue(eventNames));

      oneOf(pushChannelService).subscribe(subscriber, eventTypes);
    }});

    restService.doPut(request, response);
  }

  @Test
  public void subscribeNoEvents() throws Exception {
    final String subscriber = "test-subscriber";

    context.checking(new Expectations() {{
      allowing(request).getParameter("subscriber");
      will(returnValue(subscriber));
      allowing(request).getParameterValues("eventName");
      will(returnValue(null));
    }});

    restService.doPut(request, response);
  }

  @Test
  public void unSubscribeFromEvent() throws Exception {
    final String subscriber = "test-subscriber";
    final String eventName = "event";
    final String correlationId = "CorrelId";

    context.checking(new Expectations() {{
      allowing(request).getParameter("subscriber");
      will(returnValue(subscriber));
      allowing(request).getParameter("eventName");
      will(returnValue(eventName));
      allowing(request).getParameter("correlationId");
      will(returnValue(correlationId));

      oneOf(pushChannelService).unsubscribe(subscriber, new PushEvent.Type(eventName, correlationId));
    }});

    restService.doDelete(request, response);
  }

  @Test
  public void unSubscribeFromEventWithoutCorrelationId() throws Exception {
    final String subscriber = "test-subscriber";
    final String eventName = "event";

    context.checking(new Expectations() {{
      allowing(request).getParameter("subscriber");
      will(returnValue(subscriber));
      allowing(request).getParameter("eventName");
      will(returnValue(eventName));
      allowing(request).getParameter("correlationId");
      will(returnValue(null));

      oneOf(pushChannelService).unsubscribe(subscriber, new PushEvent.Type(eventName, ""));
    }});

    restService.doDelete(request, response);
  }

  @Test
  public void keepAlive() throws Exception {
    final String subscriber = "test-subscriber";

    context.checking(new Expectations() {{
      allowing(request).getParameter("subscriber");
      will(returnValue(subscriber));

      oneOf(pushChannelService).keepAlive(subscriber);
    }});

    restService.doPost(request, response);
  }

  class ServletOutputStreamStub extends ServletOutputStream {

    private String output;

    @Override
    public void write(byte[] b) throws IOException {
      output = new String(b);
    }

    @Override
    public void write(int b) throws IOException {
    }

    @Override
    public void flush() throws IOException {
    }

    public String getOutput() {
      return output;
    }
  }
}