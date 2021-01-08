package org.george.util;

import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;

public class MessageOuter {

  private Vertx vertx;

  private MessageOuter(){}

  private static MessageOuter instance = new MessageOuter();

  public static MessageOuter getInstance(){
    return instance;
  }

  public void addVertx(Vertx vertx){
    this.vertx = vertx;
  }

  public void out(String msg, String hId){
    if(hId != null)
      this.vertx.eventBus().send(hId, Buffer.buffer(msg + "\r\n", "GBK"));
  }
}
