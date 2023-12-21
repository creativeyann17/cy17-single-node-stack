package com.creativeyann17.app.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@RestControllerAdvice
@Slf4j
public class ErrorController {

  @ExceptionHandler(Exception.class)
  public ResponseEntity<String> handle(Exception e) {
    log.error("", e);
    return new ResponseEntity<>("internal server error", HttpStatus.INTERNAL_SERVER_ERROR);
  }

  @ExceptionHandler(ResponseStatusException.class)
  public ResponseEntity<String> handle(ResponseStatusException e) {
    return new ResponseEntity<>(e.getReason(), e.getStatusCode());
  }

  @ExceptionHandler(NoResourceFoundException.class)
  public ResponseEntity<String> handle(NoResourceFoundException e) {
    return new ResponseEntity<>("", HttpStatus.NOT_FOUND);
  }


}
