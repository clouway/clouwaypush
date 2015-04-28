package com.clouway.push.server;

import com.clouway.push.shared.PushEvent;
import com.clouway.push.shared.PushEventHandler;
import com.clouway.push.shared.util.DateTime;
import com.google.common.collect.Sets;
import com.google.inject.util.Providers;
import org.jmock.Expectations;
import org.jmock.auto.Mock;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import static com.clouway.push.server.Subscription.aNewSubscription;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * @author Ivan Lazov <ivan.lazov@clouway.com>
 */
public class ActiveSubscriptionsFilterImplTest {

  @Rule
  public final JUnitRuleMockery context = new JUnitRuleMockery();

  private ActiveSubscriptionsFilter activeSubscriptionsFilter;

  @Mock
  private SubscriptionsRepository repository;

  private DateTime expirationDate;
  private DateTime activeDate;

  private SimpleEvent event = new SimpleEvent();

  private SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

  @Before
  public void setUp() throws ParseException {

    DateTime currentDate = new DateTime(dateFormat.parse("10:00:00"));
    activeDate = new DateTime(dateFormat.parse("11:00:00"));
    expirationDate = new DateTime(dateFormat.parse("09:00:00"));

    activeSubscriptionsFilter = new ActiveSubscriptionsFilterImpl(repository, Providers.of(currentDate));
  }

  @Test
  public void filterActiveSubscriptions() {

    final Subscription activeSubscription = aNewSubscription().subscriber("john@gmail.com")
                                                              .expirationDate(activeDate)
                                                              .build();

    final Subscription expiredSubscription = aNewSubscription().subscriber("peter@gmail.com")
                                                               .expirationDate(expirationDate)
                                                               .build();

    final List<Subscription> subscriptionList = new ArrayList<Subscription>();
    subscriptionList.add(activeSubscription);
    subscriptionList.add(expiredSubscription);


    context.checking(new Expectations() {{
      oneOf(repository).findSubscriptions(event.TYPE);
      will(returnValue(subscriptionList));

      oneOf(repository).removeSubscriptions(event.TYPE, Sets.newHashSet("peter@gmail.com"));
    }});

    List<Subscription> subscriptions = activeSubscriptionsFilter.filterSubscriptions(event.TYPE);

    assertThat(subscriptions.size(), is(1));
    assertThat(subscriptions.get(0).getSubscriber(), is("john@gmail.com"));
  }

  private interface SimpleEventHandler extends PushEventHandler {
  }

  private class SimpleEvent extends PushEvent<SimpleEventHandler> {

    private Type<SimpleEventHandler> TYPE = new Type<SimpleEventHandler>();

    public Type<SimpleEventHandler> getAssociatedType() {
      return TYPE;
    }

    public void dispatch(SimpleEventHandler handler) {
    }
  }
}
