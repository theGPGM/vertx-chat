package org.george.chat_room_game.model;

import org.george.common.pojo.Message;

import java.util.List;

public interface GameModel {

    List<Message> settle(String roomId);

    Integer getPlayerNum(String roomId);
}
