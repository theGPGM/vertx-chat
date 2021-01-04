package org.george.hall.cmd;

import org.george.pojo.Message;
import org.george.pojo.Messages;
import org.george.dungeon_game.model.DungeonGameModel;
import org.george.dungeon_game.model.DungeonGameModelImpl;
import org.george.hall.cache.PlayerCache;
import org.george.hall.cache.bean.PlayerCacheBean;
import org.george.hall.dao.PlayerDao;
import org.george.hall.dao.bean.PlayerBean;
import org.george.hall.model.ClientModel;
import org.george.hall.model.impl.ClientModelImpl;

import java.util.ArrayList;
import java.util.List;

public class LoginCMDs {

    private PlayerCache playerCache = PlayerCache.getInstance();

    private PlayerDao playerDao = PlayerDao.getInstance();

    private ClientModel clientModel = ClientModelImpl.getInstance();

    private DungeonGameModel dungeonGameModel = DungeonGameModelImpl.getInstance();

    /**
     * 玩家登录
     * 此时会进行玩家的账号密码的校验
     * @param args
     * @return
     */
    public Messages login(String...args){

        List<Message> list = new ArrayList<>();
        if(args == null || args.length != 2){
            list.add(new Message(null, "输入格式错误"));
        }
        else{
            String username = args[0];
            String password = args[1];

            // 从缓存中获取
            PlayerCacheBean cacheBean = playerCache.loadPlayerByPlayerName(username);
            if(cacheBean == null){
                PlayerBean bean = playerDao.loadPlayerByPlayerName(username);
                if(bean == null){
                    list.add(new Message(null, "用户名或密码错误"));
                }else if(!bean.getPassword().equals(password)){
                    list.add(new Message(null, "用户名或密码错误"));
                }else{
                    cacheBean = bean2CacheBean(bean);
                    list.add(new Message(String.valueOf(bean.getPlayerId()), "用户登录成功"));
                    // 添加缓存
                    playerCache.addPlayer(bean2CacheBean(bean));
                    if(!playerCache.existTimeStamp(cacheBean.getPlayerId())){
                        playerCache.addTimeStamp(cacheBean.getPlayerId());
                    }
                }
            }else{
                list.add(new Message(String.valueOf(cacheBean.getPlayerId()), "用户登录成功"));
                if(!playerCache.existTimeStamp(cacheBean.getPlayerId())){
                    playerCache.addTimeStamp(cacheBean.getPlayerId());
                }
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
            list.add(new Message(null, "输入格式错误"));
        }
        else{
            String username = args[0];
            String password = args[1];

            // 从缓存中获取
            PlayerCacheBean cacheBean = playerCache.loadPlayerByPlayerName(username);
            if(cacheBean == null){
                PlayerBean bean = playerDao.loadPlayerByPlayerName(username);
                if(bean != null){
                    list.add(new Message(null, "用户已存在"));
                }else{

                    bean = new PlayerBean();
                    bean.setPassword(username);
                    bean.setPassword(password);
                    playerDao.addPlayer(bean);

                    bean = playerDao.loadPlayerByPlayerName(username);
                    playerCache.addPlayer(bean2CacheBean(bean));
                    if(!playerCache.existTimeStamp(cacheBean.getPlayerId())){
                        playerCache.addTimeStamp(cacheBean.getPlayerId());
                    }
                    list.add(new Message(String.valueOf(bean.getPlayerId()), "用户注册成功"));
                }
            }else{
                list.add(new Message(null, "用户已存在"));
            }
        }
        return new Messages(list);
    }

    /**
     * 查看玩家信息
     * ID、姓名、hp、元宝数
     * @param args
     * @return
     */
    public Messages info(String...args){
        List<Message> list = new ArrayList<>();
        Integer playerId = Integer.parseInt(args[0]);
        if(args.length != 1){
            list.add(new Message(String.valueOf(playerId),"输入格式错误"));
        } else {

            PlayerCacheBean cacheBean = playerCache.loadPlayerByPlayerId(playerId);
            if (cacheBean == null) {
                PlayerBean bean = playerDao.loadPlayerByPlayerId(playerId);
                cacheBean = bean2CacheBean(bean);
                playerCache.addPlayer(cacheBean);
            }

            StringBuilder sb = new StringBuilder();
            sb.append("=============================================================================\r\n");
            sb.append("     玩家信息:\r\n");
            sb.append("====>玩家ID:" + cacheBean.getPlayerId());
            sb.append("\r\n");
            sb.append("====>玩家名:" + cacheBean.getPlayerName());
            sb.append("\r\n");
            sb.append("====>体力:" + playerCache.getPlayerHp(cacheBean.getPlayerId()));  // 更新体力
            sb.append("\r\n");
            sb.append("====>元宝:" + cacheBean.getGold());
            sb.append("\r\n");
            sb.append("=============================================================================");
            list.add(new Message(String.valueOf(cacheBean.getPlayerId()), sb.toString()));
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
            msg = new Message(null,"输入格式错误");
        }
        else {
            clientModel.logout(userId);
            msg = new Message(null,"用户退出登录");
        }
        list.add(msg);
        return new Messages(list);
    }

    private PlayerCacheBean bean2CacheBean(PlayerBean bean){
        PlayerCacheBean cacheBean = new PlayerCacheBean();
        cacheBean.setPlayerId(bean.getPlayerId());
        cacheBean.setPlayerName(bean.getPlayerName());
        cacheBean.setPassword(bean.getPassword());
        cacheBean.setHp(bean.getHp());
        cacheBean.setGold(bean.getGold());
        return cacheBean;
    }
}
