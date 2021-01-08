package org.george.hall.cmd;


import org.george.hall.dao.PlayerAuthDao;
import org.george.hall.dao.bean.PlayerAuthBean;
import org.george.hall.model.PlayerModel;
import org.george.pojo.Message;
import org.george.pojo.Messages;
import org.george.hall.cache.PlayerCache;
import org.george.hall.cache.bean.PlayerCacheBean;
import org.george.hall.dao.PlayerDao;
import org.george.hall.dao.bean.PlayerBean;

import java.util.ArrayList;
import java.util.List;

public class LoginCmds {

    private PlayerCache playerCache = PlayerCache.getInstance();

    private PlayerDao playerDao = PlayerDao.getInstance();

    private PlayerAuthDao playerAuthDao = PlayerAuthDao.getInstance();

    private PlayerModel playerModel = PlayerModel.getInstance();

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
     * 此时会进行玩家的账号密码的校验
     * @param args
     * @return
     */
    public Messages login(String...args){

        List<Message> list = new ArrayList<>();
        if(args == null || args.length != 2){
            list.add(new Message(null, input_format_error));
        }
        else{
            String playerName = args[0];
            String password = args[1];

            PlayerAuthBean authBean = playerAuthDao.loadPlayerAuthBeanByPlayerName(playerName);
            if(authBean == null){
                list.add(new Message(null, username_or_password_wrong));
            }else if(!authBean.getPassword().equals(password)){
                list.add(new Message(null, username_or_password_wrong));
            }else{

                PlayerCacheBean cacheBean = playerCache.loadPlayerByPlayerId(authBean.getPlayerId());
                if(cacheBean == null){
                    PlayerBean bean = playerDao.loadPlayerByPlayerId(authBean.getPlayerId());
                    cacheBean = bean2CacheBean(bean);
                    playerCache.addPlayer(cacheBean);
                }

                list.add(new Message(String.valueOf(authBean.getPlayerId()), login_success));
                playerCache.addTimeStampIfNotExisting(authBean.getPlayerId());
            }
        }
        return new Messages(list);
    }

    /**
     * 玩家注册
     * @param args
     * @return
     */
    public Messages register(String...args){
        List<Message> list = new ArrayList<>();
        if(args == null || args.length != 2){
            list.add(new Message(null, input_format_error));
        }
        else{
            String username = args[0];
            String password = args[1];

            boolean result = playerAuthDao.addPlayer(username, password);
            if(!result){
                list.add(new Message(null, user_already_exists));
            }else{
                PlayerAuthBean authBean = playerAuthDao.loadPlayerAuthBeanByPlayerName(username);
                list.add(new Message(String.valueOf(authBean.getPlayerId()), register_success));

                PlayerBean bean = new PlayerBean();
                bean.setPlayerId(authBean.getPlayerId());
                bean.setPlayerName(authBean.getPlayerName());
                playerDao.addPlayer(bean);
                playerCache.addTimeStampIfNotExisting(authBean.getPlayerId());
            }
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
        Integer playerId = Integer.parseInt(args[0]);
        if(args.length != 1){
            list.add(new Message(String.valueOf(playerId),input_format_error));
        } else {

            PlayerCacheBean cacheBean = playerCache.loadPlayerByPlayerId(playerId);
            if (cacheBean == null) {
                PlayerBean bean = playerDao.loadPlayerByPlayerId(playerId);
                cacheBean = bean2CacheBean(bean);
                playerCache.addPlayer(cacheBean);
            }

            int hp = playerModel.getPlayerCurrentHP(playerId);
            int gold = cacheBean.getGold();
            String name = cacheBean.getPlayerName();
            list.add(new Message(String.valueOf(cacheBean.getPlayerId()), info2Msg(name, hp, gold)));
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
        Message msg = null;

        String userId = args[0];
        if(args.length != 1){
            msg = new Message(null,input_format_error);
        }
        else {
            playerModel.logout(userId);
            msg = new Message(null,quit_login_success);
        }
        list.add(msg);
        return new Messages(list);
    }

    private PlayerCacheBean bean2CacheBean(PlayerBean bean){
        PlayerCacheBean cacheBean = new PlayerCacheBean();
        cacheBean.setPlayerId(bean.getPlayerId());
        cacheBean.setPlayerName(bean.getPlayerName());
        cacheBean.setHp(bean.getHp());
        cacheBean.setGold(bean.getGold());
        return cacheBean;
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
