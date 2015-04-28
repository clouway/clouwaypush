package com.clouway.push.server;

import com.google.inject.BindingAnnotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Ivan Lazov <ivan.lazov@clouway.com>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.PARAMETER, ElementType.METHOD, ElementType.FIELD})
@BindingAnnotation
public @interface SubscriptionsExpirationMills {
}
