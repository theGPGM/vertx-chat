package org.george;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.net.NetServer;
import org.apache.ibatis.session.SqlSession;
import org.george.cmd.model.CmdModel;
import org.george.pojo.LevelBean;
import org.george.common.pojo.Message;
import org.george.common.pojo.Messages;
import org.george.chat_room_game.model.GameModel;
import org.george.chat_room_game.model.GameModelImpl;
import org.george.dungeon_game.model.DungeonGameModel;
import org.george.dungeon_game.model.DungeonGameModelImpl;
import org.george.hall.model.ClientModel;
import org.george.hall.model.impl.ClientModelImpl;
import org.george.util.PropertiesUtils;
import org.george.util.SessionPool;
import redis.clients.jedis.Jedis;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

public class Main extends AbstractVerticle {

  private Map<String, Object> commandClassMap = new HashMap<>();

  private Map<String, Method> commandMethodMap = new HashMap<>();

  private List<String> commandDescriptionList = new ArrayList<>();

  private ClientModel clientModel = ClientModelImpl.getInstance();

  private org.george.util.MessageOuter messageOuter = org.george.util.MessageOuter.getInstance();

  private GameModel gameModel = GameModelImpl.getInstance();

  private DungeonGameModel dungeonGameModel = DungeonGameModelImpl.getInstance();

  private CmdModel cmdModel = CmdModel.getInstance();

  public static void main(String[] args) {
    Vertx.vertx().deployVerticle(new Main());
  }

  @Override
  public void start(){

    messageOuter.addVertx(vertx);

    // 加载配置
    vertx.executeBlocking(promise -> {
      cmdModel.loadCmdDescriptionProperties(PropertiesUtils.loadProperties("src/main/resources/conf/description.properties"));
      cmdModel.loadCmdProperties(PropertiesUtils.loadProperties("src/main/resources/conf/cmds.properties"));
    });

    // 监听连接
    NetServer server = vertx.createNetServer();
    server.connectHandler(handler -> {
      String hId = handler.writeHandlerID();
      clientModel.addClient(hId,handler);
      welCome(hId);
      handler.handler(buff -> {
        // 进入 worker 线程
        vertx.executeBlocking(promise -> {
          String cmd = buff.toString("GBK");
          List<Message> list = cmdModel.execute(cmd);
          for(Message msg : list){
            if(msg.getSendToUser() != null){
              messageOuter.out(msg.getMessage(), clientModel.getHIdByUserId(msg.getSendToUser()));
            }else{
              messageOuter.out(msg.getMessage(), clientModel.getHIdByUserId(hId));
            }
          }
        });
      });

      // 客户端直接退出时，清除缓存
      handler.closeHandler(fun -> {
        vertx.executeBlocking(promise -> {
          clientModel.closeClient(hId);
          String userId = clientModel.getUserIdByHId(hId);
          if(userId != null){
            clientModel.logout(userId);
          }
        });
      });
    });

    // 启动服务
    server.listen(2233, "localhost");
  }

  private void loadProperties(){

    // 加载关卡信息
    List<LevelBean> levelBeans = org.george.util.CSVLevelReader.loadLevel("src/main/resources/csv/level.csv");
    dungeonGameModel.addLevelInfo(levelBeans);
  }

  private void welCome(String hId){
    StringBuilder sb = new StringBuilder();
    sb.append("欢迎登录,您可以使用以下命令\r\n");
    int i = 1;
    for(String description : commandDescriptionList){
      sb.append(i++).append("、").append(description).append("\r\n");
    }
    messageOuter.out(sb.toString(), hId);
  }

  /**
   * 将请求映射到对应的 command 类中
   * @param message
   * @param hId
   */
  private void mapping(String message, String hId) throws InvocationTargetException, IllegalAccessException {

    // 消息切割
    String[] strs = message.split(":");
    if(strs == null || strs.length == 0) {
      messageOuter.out("输入格式错误", hId);
    }
    else if(!commandMethodMap.containsKey(strs[0])){
      messageOuter.out("输入格式错误", hId);
    }
    else{
      // 用户要操作的方法
      String cmd = strs[0];

      // 切入 jedis
      Jedis jedis = org.george.util.JedisPool.getJedis();
      SqlSession sqlSession = org.george.util.SessionPool.openSession();
      org.george.util.ThreadLocalJedisUtils.addJedis(jedis);
      org.george.util.ThreadLocalSessionUtils.addSqlSession(sqlSession);
      try{
        // 登录
        if("login".equals(cmd) || "register".equals(cmd)){
          String[] params = new String[strs.length - 1];
          System.arraycopy(strs, 1, params, 0, strs.length - 1);
          Method method = commandMethodMap.get(cmd);
          Object o = commandClassMap.get(cmd);
          Messages msgs = (Messages) method.invoke(o, new Object[]{params});
          List<Message> list = msgs.getMessageList();
          Message msg = list.get(0);
          if(msg.getSendToUser() == null){
            messageOuter.out(msg.getMessage(), hId);
          }

          // 当前账号有人登录
          else if(clientModel.getHIdByUserId(msg.getSendToUser()) != null){
            // 不允许重复登录
            if(clientModel.getUserIdByHId(hId) != null){
              messageOuter.out("请勿重复登录", hId);
            }
            // 踢掉他
            else{
              String userHId = clientModel.getHIdByUserId(msg.getSendToUser());
              messageOuter.out("账户异地登录", userHId);
              clientModel.closeClient(userHId);
              clientModel.addUserIdHId(msg.getSendToUser(), hId);
              messageOuter.out(msg.getMessage(), hId);
            }
          }
          else{
            clientModel.addUserIdHId(msg.getSendToUser(), hId);
            messageOuter.out(msg.getMessage(), hId);
          }

          vertx.setPeriodic(10000, handler -> {

          });
        }
        // 权限校验
        else if(clientModel.getUserIdByHId(hId) == null){
          messageOuter.out("当前操作需登录", hId);
        } else{
          String userId =  clientModel.getUserIdByHId(hId);
          String [] params = new String[strs.length];
          System.arraycopy(strs, 1, params, 1, strs.length - 1);
          params[0] = userId;
          Method method = commandMethodMap.get(cmd);
          Object o = commandClassMap.get(cmd);
          Messages list = (Messages) method.invoke(o, new Object[]{params});

          if("logout".equals(cmd)){
            messageOuter.out(list.getMessageList().get(0).getMessage(), hId);
          }
          // 如果是创建游戏，需要在一段时间后处理游戏结果
          else if("create_game".equals(cmd)){
            // 正确创建了游戏
            if(list.getMessageList().size() > 1){
              for(Message msg : list.getMessageList()){
                if(msg.getSendToUser() != null && msg.getMessage() != null){
                  String userHId = clientModel.getHIdByUserId(msg.getSendToUser());
                  if(userHId != null){
                    messageOuter.out(msg.getMessage(), userHId);
                  }
                }
              }
            }

            // 10 秒后处理游戏结果
            vertx.setTimer(10000, handler -> {
              Jedis gameSetterJedis = org.george.util.JedisPool.getJedis();
              org.george.util.ThreadLocalJedisUtils.addJedis(gameSetterJedis);
              if(gameModel.getPlayerNum(params[1]) < 2){
                messageOuter.out("参与游戏人数过少，无法进行游戏", hId);
              }
              else{
                List<Message> msgs = gameModel.settle(params[1]);
                for(Message msg : msgs){
                  if(msg.getSendToUser() != null && msg.getMessage() != null){
                    String userHId = clientModel.getHIdByUserId(msg.getSendToUser());
                    if(userHId != null){
                      messageOuter.out(msg.getMessage(), userHId);
                    }
                  }
                }
              }
              org.george.util.JedisPool.returnJedis(jedis);
            });
          }
          else if(list.getMessageList() != null){
            for(Message msg : list.getMessageList()){
              if(msg.getSendToUser() != null && msg.getMessage() != null){
                String userHId = clientModel.getHIdByUserId(msg.getSendToUser());
                if(userHId != null){
                  messageOuter.out(msg.getMessage(), userHId);
                }
              }
            }
          }
        }
      } finally {
        sqlSession.commit();
        org.george.util.JedisPool.returnJedis(jedis);
        SessionPool.close(sqlSession);
      }
    }
  }
}
