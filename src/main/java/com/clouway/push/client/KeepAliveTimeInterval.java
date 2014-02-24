package com.clouway.push.client;

import com.google.inject.BindingAnnotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * KeepAliveTimeInterval class is used in the {@link com.clouway.push.client.PushChannelGinModule}
 * for configuring the time interval between each periodic request that the client sends
 * to the server in order to notify him that is still active.
 *
 * @author Ivan Lazov <ivan.lazov@clouway.com>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.PARAMETER, ElementType.METHOD, ElementType.FIELD})
@BindingAnnotation
@interface KeepAliveTimeInterval {
}
