package com.clouway.push.server;

import com.clouway.push.client.InstanceCapture;
import com.clouway.push.client.channelapi.PushChannelService;
import org.jmock.auto.Mock;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.junit.Before;
import org.junit.Rule;

/**
 * @author Ivan Lazov <ivan.lazov@clouway.com>
 */
public class PushChannelServiceImplTest {

  @Rule
  public final JUnitRuleMockery context = new JUnitRuleMockery();

  private PushChannelService pushChannelService;

  @Mock
  private SubscriptionsRepository subscriptionsRepository;

  //@Mock
  //private Subscriber subscriber;

  private InstanceCapture<Subscription> expectedSubscription;

//  private final DateTime currentDateAndTime = new DateTime(2013, 6, 5, 10, 15);
//  private final DateTime expirationDateAndTime = new DateTime(2013, 6, 5, 10, 20);

//  private final SimpleEvent event = new SimpleEvent();
  private final String subscriberName = "john@gmail.com";

//  final Subscription subscription = aNewSubscription().eventName(event.TYPE.getEventName())
//                                                      .subscriber(subscriberName)
//                                                      .expirationDateAndTime(currentDateAndTime)
//                                                      .timesSubscribed(1)
//                                                      .build();

  @Before
  public void setUp() {
//    pushChannelService = new PushChannelServiceImpl(Providers.of(subscriptionsRepository), Providers.of(subscriber), Providers.of(currentDateAndTime));
    expectedSubscription = new InstanceCapture<Subscription>();
  }
//
//  @Test
//  public void subscribeForNewEvent() {
//
//    context.checking(new Expectations() {{
//      oneOf(subscriber).getName();
//      will(returnValue(subscriberName));
//
//      oneOf(subscriptionsRepository).hasSubscription(event.TYPE, subscriberName);
//      will(returnValue(false));
//
//      oneOf(subscriptionsRepository).put(with(expectedSubscription));
//    }});
//
//    pushChannelService.subscribe(event.TYPE);
//
//    assertExpectedSubscription(expectedSubscription.getValue(), 1);
//  }
//
//  @Test
//  public void subscribeForEventTwice() {
//
//    context.checking(new Expectations() {{
//      oneOf(subscriber).getName();
//      will(returnValue(subscriberName));
//
//      oneOf(subscriptionsRepository).hasSubscription(event.TYPE, subscriberName);
//      will(returnValue(true));
//
//      oneOf(subscriptionsRepository).get(event.TYPE, subscriberName);
//      will(returnValue(subscription));
//
//      oneOf(subscriptionsRepository).put(with(expectedSubscription));
//    }});
//
//    pushChannelService.subscribe(event.TYPE);
//
//    assertExpectedSubscription(expectedSubscription.getValue(), 2);
//  }
//
//  @Test
//  public void unSubscribeFromOnceSubscribedEvent() {
//
//    context.checking(new Expectations() {{
//      oneOf(subscriber).getName();
//      will(returnValue(subscriberName));
//
//      oneOf(subscriptionsRepository).hasSubscription(event.TYPE, subscriberName);
//      will(returnValue(true));
//
//      oneOf(subscriptionsRepository).get(event.TYPE, subscriberName);
//      will(returnValue(subscription));
//
//      oneOf(subscriptionsRepository).removeSubscription(event.TYPE, subscriberName);
//    }});
//
//    pushChannelService.unsubscribe(event.TYPE);
//  }
//
//  @Test
//  public void unSubscribeFromTwiceSubscribedEvent() {
//
//    final Subscription subscription = aNewSubscription().eventName(event.TYPE.getEventName())
//                                                        .subscriber(subscriberName)
//                                                        .expirationDateAndTime(currentDateAndTime)
//                                                        .timesSubscribed(2)
//                                                        .build();
//
//    context.checking(new Expectations() {{
//      oneOf(subscriber).getName();
//      will(returnValue(subscriberName));
//
//      oneOf(subscriptionsRepository).hasSubscription(event.TYPE, subscriberName);
//      will(returnValue(true));
//
//      oneOf(subscriptionsRepository).get(event.TYPE, subscriberName);
//      will(returnValue(subscription));
//
//      oneOf(subscriptionsRepository).put(with(expectedSubscription));
//
//      never(subscriptionsRepository).removeSubscription(with(any(PushEvent.Type.class)), with(any(String.class)));
//    }});
//
//    pushChannelService.unsubscribe(event.TYPE);
//
//    assertExpectedSubscription(expectedSubscription.getValue(), 1);
//  }
//
//  private void assertExpectedSubscription(Subscription subscription, int timesSubscriber) {
//
//    assertThat(subscription.getEventName(), is(equalTo(event.TYPE.getEventName())));
//    assertThat(subscription.getSubscriber(), is(equalTo(subscriberName)));
//    assertThat(subscription.getExpirationDate(), is(equalTo(expirationDateAndTime)));
//    assertThat(subscription.getTimesSubscribed(), is(timesSubscriber));
//  }
//
//  private class SimpleEventHandler implements PushEventHandler {
//  }
//
//  private static class SimpleEvent extends PushEvent<PushEventHandler> {
//
//    private static Type<PushEventHandler> TYPE = new Type<PushEventHandler>("SimpleEvent") {};
//
//    @Override
//    public Type<PushEventHandler> getAssociatedType() {
//      return TYPE;
//    }
//
//    @Override
//    public void dispatch(PushEventHandler handler) {
//    }
//  }
}
