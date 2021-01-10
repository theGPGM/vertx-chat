package org.george.hall.cmd;


import org.george.hall.cache.PlayerAuthCache;
import org.george.hall.cache.bean.PlayerAuthCacheBean;
import org.george.hall.model.PlayerModel;
import org.george.hall.uitl.JedisPool;
import org.george.hall.uitl.ThreadLocalJedisUtils;
import org.george.cmd.pojo.Message;
import org.george.cmd.pojo.Messages;
import org.george.hall.cache.PlayerInfoCache;
import org.george.hall.cache.bean.PlayerInfoCacheBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;

import java.util.ArrayList;
import java.util.List;

public class LoginCmds {

    private PlayerInfoCache playerInfoCache = PlayerInfoCache.getInstance();

    private PlayerAuthCache playerAuthCache = PlayerAuthCache.getInstance();

    private PlayerModel playerModel = PlayerModel.getInstance();

    private Logger logger = LoggerFactory.getLogger(LoginCmds.class);

    private static final String input_format_error = "输入格式错误";

    private static final String username_or_password_wrong = "用户名或密码错误";

    private static final String login_success = new StringBuilder()
            .append("============================================================\r\n")
            .append("++++登录成功++++\r\n")
            .append("您可以使用以下命令：\r\n")
            .append("============================================================\r\n")
            .append("[logout]:退出登录\r\n")
            .append("[cmd]:查看命令\r\n")
            .append("[info]:玩家信息\r\n")
            .append("[create_room:数字[将作为房间 ID]]:创建聊天室\r\n")
            .append("[join:房间 ID]:加入聊天室\r\n")
            .append("[exit_room:房间 ID]:退出聊天室\r\n")
            .append("[start_dgame]:进入地下城\r\n")
            .append("[exit_dgame]:退出地下城\r\n")
            .append("[auctions]:查看拍卖会道具\r\n")
            .append("============================================================")
            .toString();

    private static final String register_success = new StringBuilder()
            .append("============================================================\r\n")
            .append("++++注册成功++++\r\n")
            .append("您可以使用以下命令：\r\n")
            .append("============================================================\r\n")
            .append("[logout]:退出登录\r\n")
            .append("[cmd]:查看命令\r\n")
            .append("[info]:玩家信息\r\n")
            .append("[create_room:数字[将作为房间 ID]]:创建聊天室\r\n")
            .append("[join:房间 ID]:加入聊天室\r\n")
            .append("[exit_room:房间 ID]:退出聊天室\r\n")
            .append("[start_dgame]:进入地下城\r\n")
            .append("[exit_dgame]:退出地下城\r\n")
            .append("[auctions]:查看拍卖会道具\r\n")
            .append("============================================================")
            .toString();

    private static final String quit_login_success = "退出登录成功";

    private static final String user_already_exists = "用户已存在";

    /**
     * 玩家登录
     * @param args
     * @return
     */
    public Messages login(String...args){
        Jedis jedis = JedisPool.getJedis();
        ThreadLocalJedisUtils.addJedis(jedis);
        List<Message> list = new ArrayList<>();
        try{
            if(args == null || args.length != 2){
                list.add(new Message(null, input_format_error));
            }else if(playerAuthCache.loadPlayerAuthCacheBeanByName(args[0]) == null){
                // 用户名或密码错误
                list.add(new Message(null, username_or_password_wrong));
            }else if(!args[1].equals(playerAuthCache.loadPlayerAuthCacheBeanByName(args[0]).getPassword())){
                // 密码错误
                list.add(new Message(null, username_or_password_wrong));
            } else{

                String playerName = args[0];
                PlayerAuthCacheBean cacheBean = playerAuthCache.loadPlayerAuthCacheBeanByName(playerName);
                // 添加时间戳
                playerInfoCache.addTimeStampIfNotExisting(cacheBean.getPlayerId());

                list.add(new Message(String.valueOf(cacheBean.getPlayerId()), login_success));

                logger.info("用户登录:{}, 用户ID:{}", cacheBean.getPlayerName(), cacheBean.getPlayerId());
            }
        }finally {
            JedisPool.returnJedis(jedis);
        }
        return new Messages(list);
    }

    /**
     * 玩家注册
     * @param args
     * @return
     */
    public Messages register(String...args){
        Jedis jedis = JedisPool.getJedis();
        ThreadLocalJedisUtils.addJedis(jedis);
        List<Message> list = new ArrayList<>();
        try{
            if(args == null || args.length != 2){
                list.add(new Message(null, input_format_error));
            } else if(playerAuthCache.loadPlayerAuthCacheBeanByName(args[0]) != null){
                // 存在用户
                list.add(new Message(null, user_already_exists));
            } else{
                String username = args[0];
                String password = args[1];

                // 添加用户
                PlayerAuthCacheBean cacheBean = new PlayerAuthCacheBean();
                cacheBean.setPlayerName(username);
                cacheBean.setPassword(password);
                playerAuthCache.addPlayer(cacheBean);
                cacheBean = playerAuthCache.loadPlayerAuthCacheBeanByName(username);

                // 添加用户信息
                PlayerInfoCacheBean playerInfoCacheBean = new PlayerInfoCacheBean();
                playerInfoCacheBean.setPlayerId(cacheBean.getPlayerId());
                playerInfoCacheBean.setPlayerName(cacheBean.getPlayerName());
                playerInfoCacheBean.setGold(0);
                playerInfoCacheBean.setHp(100);
                playerInfoCache.addPlayer(playerInfoCacheBean);
                // 添加时间戳
                playerInfoCache.addTimeStampIfNotExisting(cacheBean.getPlayerId());

                // 返回消息
                list.add(new Message(String.valueOf(cacheBean.getPlayerId()), register_success));

                // 日志
                logger.info("用户注册: 用户名:{}, 用户 ID:{}", cacheBean.getPlayerName(), cacheBean.getPlayerId());
            }
        }finally {
            JedisPool.returnJedis(jedis);
        }
        return new Messages(list);
    }

    /**
     * 查看玩家信息
     * @param args
     * @return
     */
    public Messages info(String...args){
        List<Message> list = new ArrayList<>();
        Jedis jedis = JedisPool.getJedis();
        ThreadLocalJedisUtils.addJedis(jedis);
        try{
            if(args.length != 1){
                list.add(new Message(args[0],input_format_error));
            } else {
                PlayerInfoCacheBean cacheBean = playerInfoCache.loadPlayerByPlayerId(Integer.parseInt(args[0]));
                int hp = cacheBean.getHp();
                int gold = cacheBean.getGold();
                String name = cacheBean.getPlayerName();
                list.add(new Message(String.valueOf(cacheBean.getPlayerId()), info2Msg(name, hp, gold)));
            }
        }finally {
            JedisPool.returnJedis(jedis);
        }
        return new Messages(list);
    }

    /**
     * 用户登出
     * @param args
     * @return
     */
    public Messages logout(String...args){
        List<Message> list = new ArrayList<>();
        Jedis jedis = JedisPool.getJedis();
        ThreadLocalJedisUtils.addJedis(jedis);
        try{
            if(args.length != 1){
                list.add(new Message(null,input_format_error));
            } else {
                playerModel.logout(args[0]);
                list.add(new Message(null,quit_login_success));
            }
        }finally {
            JedisPool.returnJedis(jedis);
        }
        return new Messages(list);
    }

    private String info2Msg(String name, Integer hp, Integer gold){
        StringBuilder sb = new StringBuilder();
        sb.append("=============================================================================\r\n");
        sb.append("     玩家信息:\r\n");
        sb.append("====>玩家名:" + name);
        sb.append("\r\n");
        sb.append("====>体力:" + hp);  // 更新体力
        sb.append("\r\n");
        sb.append("====>金币:" + gold);
        sb.append("\r\n");
        sb.append("=============================================================================");
        return sb.toString();
    }
}
