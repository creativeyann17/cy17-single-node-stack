package com.creativeyann17.app.utils;

import lombok.RequiredArgsConstructor;

public class Const {

  public enum RequestAttr {
    x_req_start,
    x_req_is_system,
  }

  @RequiredArgsConstructor
  public enum RequestHeader {
    X_API_KEY("X-API-KEY"),
    X_Real_IP("X-Real-IP");
    public final String value;
  }

}
