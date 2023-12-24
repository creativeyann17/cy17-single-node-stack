package com.creativeyann17.server.handlers;

import com.creativeyann17.server.HttpResponseException;
import com.creativeyann17.server.context.Context;
import org.eclipse.jetty.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ErrorHandler {

  private static final Logger log = LoggerFactory.getLogger(ErrorHandler.class);

  public void handle(Exception e, Context ctx) {
    if (e instanceof HttpResponseException re) {
      ctx.empty(re.code);
    } else {
      log.error("", e);
      ctx.empty(HttpStatus.INTERNAL_SERVER_ERROR_500);
    }
  }


}
