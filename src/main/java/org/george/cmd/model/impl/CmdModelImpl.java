//package org.george.cmd.model.impl;
//
//import org.apache.ibatis.session.SqlSession;
//import org.george.cmd.cache.CmdCache;
//import org.george.cmd.model.CmdModel;
//import org.george.common.pojo.Message;
//import org.george.common.pojo.Messages;
//import org.george.config.CmdDescConfig;
//import org.george.hall.model.ClientModel;
//import org.george.util.SessionPool;
//import redis.clients.jedis.Jedis;
//
//import java.lang.reflect.InvocationTargetException;
//import java.lang.reflect.Method;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Properties;
//
//public class CmdModelImpl implements CmdModel {
//
//    private static CmdModelImpl instance = new CmdModelImpl();
//
//    private CmdModelImpl(){}
//
//    private CmdCache cmdCache = CmdCache.getInstance();
//
//    private ClientModel clientModel = ClientModel.getInstance();
//
//    private CmdDescConfig cmdDescConfig = CmdDescConfig.getInstance();
//
//    @Override
//    public void loadCmdProperties(Properties properties) {
//        for(String cmd : properties.stringPropertyNames()){
//            String cmdClazz = properties.getProperty(cmd);
//            try {
//                String[] split = cmdClazz.split("\\.");
//                String m = split[split.length - 1];
//
//                StringBuilder sb = new StringBuilder();
//                for(int i = 0; i < split.length - 1; i++){
//                    if(i == split.length - 2){
//                        sb.append(split[i]);
//                    }else{
//                        sb.append(split[i]);
//                        sb.append(".");
//                    }
//                }
//
//                Method method = Class.forName(sb.toString()).getDeclaredMethod(m, String[].class);
//                cmdCache.addCmdClassObj(cmd, Class.forName(sb.toString()).newInstance());
//                cmdCache.addCmdMethod(cmd, method);
//            } catch (ClassNotFoundException e) {
//                e.printStackTrace();
//            } catch (NoSuchMethodException e) {
//                e.printStackTrace();
//            } catch (IllegalAccessException e) {
//                e.printStackTrace();
//            } catch (InstantiationException e) {
//                e.printStackTrace();
//            }
//        }
//    }
//
//    @Override
//    public void loadCmdDescriptionProperties(Properties cmdDescriptionProperties) {
//        for(String key : cmdDescriptionProperties.stringPropertyNames()){
//            cmdDescConfig.addCmdDescription(key, cmdDescriptionProperties.getProperty(key));
//        }
//    }
//
//    @Override
//    public List<Message> execute(String message) {
//        return null;
//    }
//
//    @Override
//    public List<Message> execute(String hId, String message) {
//
//        List<Message> list = new ArrayList<>();
//        // 消息切割
//        String[] strs = message.split(":");
//        if(strs == null || strs.length == 0) {
//            list.add(new Message( null, "输入格式错误"));
//            return list;
//        }else if(cmdCache.getCmdMethod(strs[0]) == null){
//            list.add(new Message( null, "输入格式错误"));
//        } else{
//            // 用户要操作的方法
//            String cmd = strs[0];
//            Method method = cmdCache.getCmdMethod(cmd);
//            Object cmdObj = cmdCache.getCmdClassObj(cmd);
//
//            // 切入 jedis
//            Jedis jedis = org.george.util.JedisPool.getJedis();
//            SqlSession sqlSession = org.george.util.SessionPool.openSession();
//            org.george.util.ThreadLocalJedisUtils.addJedis(jedis);
//            org.george.util.ThreadLocalSessionUtils.addSqlSession(sqlSession);
//            try{
//                // 登录
//                if("login".equals(cmd) || "register".equals(cmd)){
//                    String[] params = new String[strs.length - 1];
//                    System.arraycopy(strs, 1, params, 0, strs.length - 1);
//                    Messages msgs = (Messages) method.invoke(cmdObj, new Object[]{params});
//                    List<Message> list = msgs.getMessageList();
//                    Message msg = list.get(0);
//                    if(msg.getSendToUser() == null){
//                        messageOuter.out(msg.getMessage(), hId);
//                    }
//
//                    // 当前账号有人登录
//                    else if(clientModel.getHIdByUserId(msg.getSendToUser()) != null){
//                        // 不允许重复登录
//                        if(clientModel.getUserIdByHId(hId) != null){
//                            messageOuter.out("请勿重复登录", hId);
//                        }
//                        // 踢掉他
//                        else{
//                            String userHId = clientModel.getHIdByUserId(msg.getSendToUser());
//                            messageOuter.out("账户异地登录", userHId);
//                            clientModel.closeClient(userHId);
//                            clientModel.addUserIdHId(msg.getSendToUser(), hId);
//                            messageOuter.out(msg.getMessage(), hId);
//                        }
//                    }
//                    else{
//                        clientModel.addUserIdHId(msg.getSendToUser(), hId);
//                        messageOuter.out(msg.getMessage(), hId);
//                    }
//
//                    vertx.setPeriodic(10000, handler -> {
//
//                    });
//                }
//                // 权限校验
//                else if(clientModel.getUserIdByHId(hId) == null){
//                    messageOuter.out("当前操作需登录", hId);
//                } else{
//                    String userId =  clientModel.getUserIdByHId(hId);
//                    String [] params = new String[strs.length];
//                    System.arraycopy(strs, 1, params, 1, strs.length - 1);
//                    params[0] = userId;
//                    Method method = commandMethodMap.get(cmd);
//                    Object o = commandClassMap.get(cmd);
//                    Messages list = (Messages) method.invoke(o, new Object[]{params});
//
//                    if("logout".equals(cmd)){
//                        messageOuter.out(list.getMessageList().get(0).getMessage(), hId);
//                    }
//                    // 如果是创建游戏，需要在一段时间后处理游戏结果
//                    else if("create_game".equals(cmd)){
//                        // 正确创建了游戏
//                        if(list.getMessageList().size() > 1){
//                            for(Message msg : list.getMessageList()){
//                                if(msg.getSendToUser() != null && msg.getMessage() != null){
//                                    String userHId = clientModel.getHIdByUserId(msg.getSendToUser());
//                                    if(userHId != null){
//                                        messageOuter.out(msg.getMessage(), userHId);
//                                    }
//                                }
//                            }
//                        }
//
//                        // 10 秒后处理游戏结果
//                        vertx.setTimer(10000, handler -> {
//                            Jedis gameSetterJedis = org.george.util.JedisPool.getJedis();
//                            org.george.util.ThreadLocalJedisUtils.addJedis(gameSetterJedis);
//                            if(gameModel.getPlayerNum(params[1]) < 2){
//                                messageOuter.out("参与游戏人数过少，无法进行游戏", hId);
//                            }
//                            else{
//                                List<Message> msgs = gameModel.settle(params[1]);
//                                for(Message msg : msgs){
//                                    if(msg.getSendToUser() != null && msg.getMessage() != null){
//                                        String userHId = clientModel.getHIdByUserId(msg.getSendToUser());
//                                        if(userHId != null){
//                                            messageOuter.out(msg.getMessage(), userHId);
//                                        }
//                                    }
//                                }
//                            }
//                            org.george.util.JedisPool.returnJedis(jedis);
//                        });
//                    }
//                    else if(list.getMessageList() != null){
//                        for(Message msg : list.getMessageList()){
//                            if(msg.getSendToUser() != null && msg.getMessage() != null){
//                                String userHId = clientModel.getHIdByUserId(msg.getSendToUser());
//                                if(userHId != null){
//                                    messageOuter.out(msg.getMessage(), userHId);
//                                }
//                            }
//                        }
//                    }
//                }
//            } catch (IllegalAccessException e) {
//                e.printStackTrace();
//            } catch (InvocationTargetException e) {
//                e.printStackTrace();
//            } finally {
//                sqlSession.commit();
//                org.george.util.JedisPool.returnJedis(jedis);
//                SessionPool.close(sqlSession);
//            }
//        }
//    }
//
//    @Override
//    public void showCmds() {
//
//    }
//
//    public static CmdModelImpl getInstance(){
//        return instance;
//    }
//}
