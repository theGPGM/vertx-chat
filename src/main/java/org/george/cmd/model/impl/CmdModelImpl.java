package org.george.cmd.model.impl;

import org.george.cmd.cache.CmdCache;
import org.george.cmd.model.CmdModel;
import org.george.cmd.model.pojo.CmdMessageResult;
import org.george.config.bean.CmdDescConfigBean;
import org.george.hall.model.PlayerModel;
import org.george.core.pojo.Message;
import org.george.core.pojo.Messages;
import org.george.config.CmdDescConfig;
import org.george.util.JedisPool;
import org.george.util.ThreadLocalJedisUtils;
import redis.clients.jedis.Jedis;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class CmdModelImpl implements CmdModel {

    private static CmdModelImpl instance = new CmdModelImpl();

    private CmdModelImpl(){}

    public static CmdModelImpl getInstance(){
        return instance;
    }

    private CmdCache cmdCache = CmdCache.getInstance();

    private PlayerModel playerModel = PlayerModel.getInstance();

    private CmdDescConfig cmdDescConfig = CmdDescConfig.getInstance();

    @Override
    public void loadCmdProperties(Properties properties) {
        for(String cmd : properties.stringPropertyNames()){
            String cmdClazz = properties.getProperty(cmd);
            try {
                String[] split = cmdClazz.split("\\.");
                String m = split[split.length - 1];

                StringBuilder sb = new StringBuilder();
                for(int i = 0; i < split.length - 1; i++){
                    if(i == split.length - 2){
                        sb.append(split[i]);
                    }else{
                        sb.append(split[i]);
                        sb.append(".");
                    }
                }

                Method method = Class.forName(sb.toString()).getDeclaredMethod(m, String[].class);
                cmdCache.addCmdClassObj(cmd, Class.forName(sb.toString()).newInstance());
                cmdCache.addCmdMethod(cmd, method);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void loadCmdDescriptionProperties(Properties cmdDescriptionProperties) {
        for(String key : cmdDescriptionProperties.stringPropertyNames()){
            cmdDescConfig.addCmdDescription(key, cmdDescriptionProperties.getProperty(key));
        }
    }

    /**
     * 执行完用户的命令之后将消息返回给用户
     * @param hId
     * @param message
     * @return
     */
    @Override
    public List<CmdMessageResult> execute(String hId, String message) {

        List<CmdMessageResult> list = new ArrayList<>();
        // 消息切割
        String[] strs = message.split(":");
        if(strs == null || strs.length == 0) {
            list.add(new CmdMessageResult(hId, "输入格式错误"));
        } else{

            String cmd = strs[0];
            Method method = cmdCache.getCmdMethod(cmd);
            Object cmdObj = cmdCache.getCmdClassObj(cmd);

            Jedis jedis = org.george.util.JedisPool.getJedis();
            ThreadLocalJedisUtils.addJedis(jedis);
            try{
                 if("login".equals(cmd) || "register".equals(cmd)){

                    if(playerModel.getUId(hId) != null){
                        list.add(new CmdMessageResult(hId, "请勿重复登录"));
                    }else{
                        String[] params = new String[strs.length - 1];
                        System.arraycopy(strs, 1, params, 0, strs.length - 1);
                        Messages msgs = (Messages) method.invoke(cmdObj, new Object[]{params});
                        Message msg = msgs.getMessageList().get(0);

                        String userId = msg.getSendToUser();

                        if(userId == null) {
                            // 登录失败
                            list.add(new CmdMessageResult(hId, msg.getMessage()));
                            return list;
                        }

                        // 认证成功
                        if(playerModel.getHId(userId) != null){
                            // 账户异地登录
                            String userHId = playerModel.getHId(userId);
                            playerModel.logout(userId);
                            list.add(new CmdMessageResult(userHId, "账户异地登录"));
                            list.add(new CmdMessageResult(hId, msg.getMessage()));
                            playerModel.addUIdAndHId(userId, hId);
                        }else{
                            playerModel.addUIdAndHId(userId, hId);
                            list.add(new CmdMessageResult(hId, msg.getMessage()));
                        }
                    }
                } else if(playerModel.getUId(hId) == null){
                    // 权限校验
                    list.add(new CmdMessageResult(hId, "当前操作需登录"));
                } else if(cmd.equals("cmd")) {
                     StringBuilder sb = new StringBuilder();
                     sb.append("============================================================\r\n")
                             .append("以下为全部命令:\r\n")
                             .append("============================================================\r\n");
                     for(CmdDescConfigBean bean : getCmdDescriptions()){
                         sb.append(bean.getDesc());
                         sb.append("\r\n");
                     }
                     sb.append("============================================================");
                     list.add(new CmdMessageResult(hId, sb.toString()));
                 }
                 else if(method == null || cmdObj == null){
                     // 没有的命令
                     list.add(new CmdMessageResult(hId, "无此命令"));
                 } else{
                    String userId =  playerModel.getUId(hId);
                    String [] params = new String[strs.length];
                    System.arraycopy(strs, 1, params, 1, strs.length - 1);
                    params[0] = userId;
                    Messages msgs = (Messages) method.invoke(cmdObj, new Object[]{params});

                    for(Message msg : msgs.getMessageList()){
                        if(msg.getSendToUser() == null){
                            list.add(new CmdMessageResult(hId, msg.getMessage()));
                        }else{
                            list.add(new CmdMessageResult(playerModel.getHId(msg.getSendToUser()), msg.getMessage()));
                        }
                    }
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } finally {
                JedisPool.returnJedis(jedis);
            }
        }
        return list;
    }

    @Override
    public List<CmdDescConfigBean> getCmdDescriptions() {
        return cmdDescConfig.getAllCmdDescriptionBeans();
    }
}
