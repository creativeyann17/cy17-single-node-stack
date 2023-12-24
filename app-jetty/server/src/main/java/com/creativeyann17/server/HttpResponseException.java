package com.creativeyann17.server;

public class HttpResponseException extends RuntimeException {

  public final int code;

  public HttpResponseException(int code) {
    super("");
    this.code = code;
  }

  public HttpResponseException(int code, String message) {
    super(message);
    this.code = code;
  }

}
