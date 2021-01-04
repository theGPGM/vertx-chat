package org.george;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.net.NetServer;
import org.george.cmd.model.CmdModel;
import org.george.cmd.model.bean.CmdMessageResult;
import org.george.config.LevelInfoConfig;
import org.george.config.bean.CmdDescConfigBean;
import org.george.dungeon_game.model.DungeonGameModel;
import org.george.hall.ClientCloseHandler;
import org.george.hall.model.ClientModel;
import org.george.hall.model.impl.ClientModelImpl;
import org.george.util.JFinalUtils;
import org.george.util.MessageOuter;
import org.george.util.PropertiesUtils;
import java.util.*;

public class Main extends AbstractVerticle {

  private ClientModel clientModel = ClientModelImpl.getInstance();

  private DungeonGameModel dungeonGameModel = DungeonGameModel.getInstance();

  private MessageOuter messageOuter = MessageOuter.getInstance();

  private CmdModel cmdModel = CmdModel.getInstance();

  private LevelInfoConfig levelInfoConfig = LevelInfoConfig.getInstance();

  public static void main(String[] args) {
    Vertx.vertx().deployVerticle(new Main());
  }

  @Override
  public void start(){

    messageOuter.addVertx(vertx);

    // 加载配置
    vertx.executeBlocking(promise -> {
      JFinalUtils.initJFinalConfig();
      cmdModel.loadCmdDescriptionProperties(PropertiesUtils.loadProperties("src/main/resources/conf/description.properties"));
      cmdModel.loadCmdProperties(PropertiesUtils.loadProperties("src/main/resources/conf/cmds.properties"));
      levelInfoConfig.loadLevelInfo("src/main/resources/csv/level.csv");

      // 观察者模式，添加客户端关闭事件观察者
      ClientCloseHandler.addObserver(dungeonGameModel);
      ClientCloseHandler.addObserver(clientModel);
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
        }, res -> {
          res.cause().printStackTrace();
        });
      });

      // 客户端直接退出时，清除缓存
      handler.closeHandler(fun -> {
        vertx.executeBlocking(promise -> {
          ClientCloseHandler.notify(hId);
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
    for(CmdDescConfigBean bean : cmdModel.getCmdDescriptions()){
      sb.append(bean.getCmd());
      sb.append(":");
      sb.append(bean.getDesc());
      sb.append("\r\n");
    }
    messageOuter.out(sb.toString(), hId);
  }
}
