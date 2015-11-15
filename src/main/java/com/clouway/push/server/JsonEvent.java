package com.clouway.push.server;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * JsonEvent is an annotation which is a marker for {@link com.clouway.push.shared.PushEvent} which will be serialised
 * as JSON.
 * <p/>
 * For example:
 * <pre>
 *   {@literal @}JsonEvent
 *   class AddPersonEvent extends PushEvent<AddPersonEventHandler>
 * </pre>
 *
 * @author Miroslav Genov (miroslav.genov@clouway.com)
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface JsonEvent {
}
