package org.george.mahjong.cmd;

import org.george.cmd.pojo.Message;
import org.george.cmd.pojo.Messages;
import org.george.dungeon_game.util.JedisPool;
import org.george.dungeon_game.util.ThreadLocalJedisUtils;
import redis.clients.jedis.Jedis;

import java.util.ArrayList;
import java.util.List;

public class MahJongCmds {

    private static final String input_format_error = "输入格式错误";

    public Messages createMahJongRoom(String...args){
        List<Message> list = new ArrayList<>();

        Jedis jedis = JedisPool.getJedis();
        ThreadLocalJedisUtils.addJedis(jedis);
        try {
            String userId = args[0];
            Integer playerId = Integer.parseInt(args[0]);
            if (args.length != 2) {
                list.add(new Message(userId, input_format_error))
            }else if()
        }finally {
            JedisPool.returnJedis(jedis);
        }
        return new Messages(list);
    }
}
