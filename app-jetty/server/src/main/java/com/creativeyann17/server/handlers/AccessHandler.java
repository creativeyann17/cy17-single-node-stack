package com.creativeyann17.server.handlers;

import com.creativeyann17.server.context.Context;

import java.util.List;

@FunctionalInterface
public interface AccessHandler {

  List<? extends RouteRole> apply(Context context);
}
