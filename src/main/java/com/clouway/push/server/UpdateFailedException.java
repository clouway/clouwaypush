package com.clouway.push.server;

/**
 *
 * @author Miroslav Genov (miroslav.genov@clouway.com)
 */
public class UpdateFailedException extends RuntimeException {

  public UpdateFailedException(String msg) {
    super(msg);
  }
}
