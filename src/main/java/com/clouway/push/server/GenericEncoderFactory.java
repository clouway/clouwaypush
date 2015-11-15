package com.clouway.push.server;

import com.clouway.push.shared.PushEvent;

import java.lang.annotation.Annotation;

/**
 * GenericEncoderFactory is an {@link EncoderFactory} which is creating different kind of encoders depending on the event.
 * <p/>
 * For events which are annotated with {@link JsonEvent}, factory creates JSON encoder, where in other case it uses the default
 * GWT-RPC encoder.
 * <p/>
 *
 *
 * @author Miroslav Genov (miroslav.genov@clouway.com)
 */
class GenericEncoderFactory implements EncoderFactory {

  private final Encoder rpcEncoder;
  private final Encoder jsonEncoder;

  public GenericEncoderFactory(Encoder rpcEncoder,
                               Encoder jsonEncoder) {

    this.rpcEncoder = rpcEncoder;
    this.jsonEncoder = jsonEncoder;
  }

  @Override
  public Encoder create(PushEvent e) {

    if (isJsonEvent(e)) {
      return jsonEncoder;
    }

    return rpcEncoder;
  }

  private boolean isJsonEvent(PushEvent e) {
    Annotation annotation = e.getClass().getAnnotation(JsonEvent.class);
    return annotation != null;
  }
}
