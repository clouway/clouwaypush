package com.clouway.push.server;

import com.clouway.push.shared.PushEvent;
import com.google.inject.ImplementedBy;

import java.util.List;

/**
 * @author Ivan Lazov <ivan.lazov@clouway.com>
 */
@ImplementedBy(ActiveSubscriptionsFilterImpl.class  )
public interface ActiveSubscriptionsFilter {

  List<Subscription> filterSubscriptions(PushEvent.Type type);
}
