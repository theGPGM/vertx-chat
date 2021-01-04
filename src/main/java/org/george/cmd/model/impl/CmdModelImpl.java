package org.george.cmd.model.impl;

import org.apache.ibatis.session.SqlSession;
import org.george.cmd.cache.CmdCache;
import org.george.cmd.model.CmdModel;
import org.george.cmd.model.bean.CmdMessageResult;
import org.george.common.pojo.Message;
import org.george.common.pojo.Messages;
import org.george.config.CmdDescConfig;
import org.george.hall.model.ClientModel;
import org.george.util.SessionPool;
import redis.clients.jedis.Jedis;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class CmdModelImpl implements CmdModel {

    private static CmdModelImpl instance = new CmdModelImpl();

    private CmdModelImpl(){}

    private CmdCache cmdCache = CmdCache.getInstance();

    private ClientModel clientModel = ClientModel.getInstance();

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
            SqlSession sqlSession = org.george.util.SessionPool.openSession();
            org.george.util.ThreadLocalJedisUtils.addJedis(jedis);
            org.george.util.ThreadLocalSessionUtils.addSqlSession(sqlSession);
            try{
                // 登录
                if("login".equals(cmd) || "register".equals(cmd)){

                    if(clientModel.getUserIdByHId(hId) != null){
                        list.add(new CmdMessageResult(hId, "请勿重复登录"));
                    }else{
                        String[] params = new String[strs.length - 1];
                        System.arraycopy(strs, 1, params, 0, strs.length - 1);
                        Messages msgs = (Messages) method.invoke(cmdObj, new Object[]{params});
                        Message msg = msgs.getMessageList().get(0);

                        String userId = msg.getSendToUser();

                        if(userId == null) {
                            list.add(new CmdMessageResult(hId, msg.getMessage()));
                        }else if(clientModel.getHIdByUserId(userId) != null){
                            // 账户异地登录
                            String userHId = clientModel.getHIdByUserId(userId);
                            clientModel.logout(userId);
                            list.add(new CmdMessageResult(userHId, "账户异地登录"));
                            clientModel.addUserIdHId(userId, hId);
                        }else{
                            // 正常登录
                            clientModel.addUserIdHId(userId, hId);
                            list.add(new CmdMessageResult(hId, msg.getMessage()));
                        }
                    }
                } else if(clientModel.getUserIdByHId(hId) == null){
                    // 权限校验
                    list.add(new CmdMessageResult(hId, "当前操作需登录"));
                } else{
                    String userId =  clientModel.getUserIdByHId(hId);
                    String [] params = new String[strs.length];
                    System.arraycopy(strs, 1, params, 1, strs.length - 1);
                    params[0] = userId;
                    Messages msgs = (Messages) method.invoke(cmdObj, new Object[]{params});

                    for(Message msg : msgs.getMessageList()){
                        if(msg.getSendToUser() == null){
                            list.add(new CmdMessageResult(hId, msg.getMessage()));
                        }else{
                            list.add(new CmdMessageResult(clientModel.getHIdByUserId(msg.getSendToUser()), msg.getMessage()));
                        }
                    }
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } finally {
                sqlSession.commit();
                org.george.util.JedisPool.returnJedis(jedis);
                SessionPool.close(sqlSession);
            }
        }
        return list;
    }

    public static CmdModelImpl getInstance(){
        return instance;
    }
}
