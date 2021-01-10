package org.george.core.util;

import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;

public class MessageOuter {

    private static MessageOuter instance = new MessageOuter();

    public static MessageOuter getInstance(){
        return instance;
    }

    private MessageOuter(){}

    private Vertx vertx;

    public void addVertx(Vertx vertx){
        this.vertx = vertx;
    }

    public void out(String hId, String message){
        if(hId != null && hId.length() != 0){
            this.vertx.eventBus().send(hId, Buffer.buffer(message + "\r\n", "GBK"));
        }
    }
}
