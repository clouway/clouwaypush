package com.clouway.push.client;

import com.google.inject.BindingAnnotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * ChanelReconnectTimeInterval class is used in the {@link PushChannelGinModule}
 * for configuring the time interval for reconnect to server when establishing of connection is failed.
 *
 * @author @author Georgi Georgiev (GeorgievJon@gmail.com)
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.PARAMETER, ElementType.METHOD, ElementType.FIELD})
@BindingAnnotation
@interface ChanelReconnectTimeInterval {
}
