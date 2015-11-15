package com.clouway.push.server;

import com.clouway.push.shared.PushEvent;

/**
 * EncoderFactory is responsible for creating of different kind of encoders depending on the event.
 * <p/>
 *
 * @author Miroslav Genov (miroslav.genov@clouway.com)
 */
interface EncoderFactory {

  /**
   * Creates new encoder for the provided event.
   * @param e the encoder that will be used for encoding of the event
   * @return the newly created or existing encoder
   */
  Encoder create(PushEvent e);

}
