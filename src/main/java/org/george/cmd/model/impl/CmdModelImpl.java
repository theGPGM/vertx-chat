package org.george.cmd.model.impl;

import org.george.cmd.cache.CmdCache;
import org.george.cmd.config.CmdDescConfig;
import org.george.cmd.config.bean.CmdDescConfigBean;
import org.george.cmd.model.CmdModel;
import org.george.cmd.model.pojo.CmdMessageResult;
import org.george.hall.model.PlayerModel;
import org.george.cmd.pojo.Message;
import org.george.cmd.pojo.Messages;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class CmdModelImpl implements CmdModel {

    private static CmdModelImpl instance = new CmdModelImpl();

    private CmdModelImpl(){}

    public static CmdModelImpl getInstance(){
        return instance;
    }

    private CmdCache cmdCache = CmdCache.getInstance();

    private PlayerModel playerModel = PlayerModel.getInstance();

    private CmdDescConfig cmdDescConfig = CmdDescConfig.getInstance();

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
                 } else if(method == null || cmdObj == null){
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
            }
        }
        return list;
    }

    @Override
    public List<CmdDescConfigBean> getCmdDescriptions() {
        return cmdDescConfig.getAllCmdDescriptionBeans();
    }
}
