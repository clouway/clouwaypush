package com.clouway.push.server;

import com.clouway.push.client.channelapi.PushChannelService;
import com.clouway.push.shared.PushEvent;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author Stefan Dimitrov (stefan.dimitrov@clouway.com)
 */
@Singleton
public class PushChannelRestService extends HttpServlet {

  private PushChannelService pushChannelService;

  @Inject
  public PushChannelRestService(PushChannelService pushChannelService) {
    this.pushChannelService = pushChannelService;
  }


  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    String subscriber = req.getParameter("subscriber");

    String channelToken = pushChannelService.connect(subscriber);

    ServletOutputStream respOutputStream = resp.getOutputStream();
    respOutputStream.write(channelToken.getBytes());
    respOutputStream.flush();
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    String subscriber = req.getParameter("subscriber");

    pushChannelService.keepAlive(subscriber);
  }

  @Override
  protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    String subscriber = req.getParameter("subscriber");
    String eventName = req.getParameter("eventName");
    String correlationId = req.getParameter("correlationId");

    pushChannelService.subscribe(subscriber, new PushEvent.Type(eventName, Strings.nullToEmpty(correlationId)));
  }

  @Override
  protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    String subscriber = req.getParameter("subscriber");
    String eventName = req.getParameter("eventName");
    String correlationId = req.getParameter("correlationId");

    pushChannelService.unsubscribe(subscriber, new PushEvent.Type(eventName, Strings.nullToEmpty(correlationId)));
  }
}
