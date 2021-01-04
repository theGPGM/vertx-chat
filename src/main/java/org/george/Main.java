package org.george;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.net.NetServer;
import org.apache.ibatis.session.SqlSession;
import org.george.cmd.model.CmdModel;
import org.george.cmd.model.bean.CmdMessageResult;
import org.george.pojo.LevelBean;
import org.george.common.pojo.Message;
import org.george.common.pojo.Messages;
import org.george.chat_room_game.model.GameModel;
import org.george.chat_room_game.model.impl.GameModelImpl;
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
          List<CmdMessageResult> list = cmdModel.execute(hId, cmd);
          for(CmdMessageResult msg : list){
            messageOuter.out(msg.getMessage(), msg.gethId());
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

  private void welCome(String hId){
    StringBuilder sb = new StringBuilder();
    sb.append("欢迎登录,您可以使用以下命令\r\n");
    int i = 1;
    for(String description : commandDescriptionList){
      sb.append(i++).append("、").append(description).append("\r\n");
    }
    messageOuter.out(sb.toString(), hId);
  }
}
