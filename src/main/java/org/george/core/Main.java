package org.george.core;

import io.vertx.core.AbstractVerticle;


import io.vertx.core.Vertx;
import io.vertx.core.net.NetServer;
import org.george.auction.DeductionHandler;
import org.george.auction.DeliveryHandler;
import org.george.chat.model.ChatRoomModel;
import org.george.cmd.model.CmdModel;
import org.george.cmd.model.pojo.CmdMessageResult;
import org.george.config.AuctionConfig;
import org.george.dungeon_game.model.DungeonGameModel;
import org.george.hall.ClientCloseHandler;
import org.george.hall.model.ClientModel;
import org.george.hall.model.PlayerModel;
import org.george.item.model.ItemModel;
import org.george.auction.pojo.AuctionTypeEnum;
import org.george.auction.pojo.DeductionTypeEnum;


import org.george.util.*;
import redis.clients.jedis.Jedis;

import java.util.*;

public class Main extends AbstractVerticle {

  private ClientModel clientModel = ClientModel.getInstance();

  private PlayerModel playerModel = PlayerModel.getInstance();

  private DungeonGameModel dungeonGameModel = DungeonGameModel.getInstance();

  private MessageOuter messageOuter = MessageOuter.getInstance();

  private CmdModel cmdModel = CmdModel.getInstance();

  private AuctionConfig auctionConfig = AuctionConfig.getInstance();

  private ChatRoomModel roomModel = ChatRoomModel.getInstance();

  public static void main(String[] args) {
    Vertx.vertx().deployVerticle(new Main());
  }

  @Override
  public void start(){

    vertx.executeBlocking(promise -> {

      // 初始化消息传递类
      messageOuter.addVertx(vertx);

      // 预加载 JFinal 的配置
      JFinalUtils.initJFinalConfig();

      // 将所有与 cmd 相关的数据加载到内存中
      cmdModel.loadCmdDescriptionProperties(PropertiesUtils.loadProperties("src/main/resources/conf/description.properties"));
      cmdModel.loadCmdProperties(PropertiesUtils.loadProperties("src/main/resources/conf/cmds.properties"));

      // 观察者模式，添加客户端关闭事件观察者，用于在客户端强制关闭时，清除缓存
      ClientCloseHandler.addObserver(roomModel);
      ClientCloseHandler.addObserver(dungeonGameModel);
      ClientCloseHandler.addObserver(playerModel);
      ClientCloseHandler.addObserver(clientModel);

      // 观察者模式，添加货币扣减事件观察者，用于金币等货币单位的扣减
      DeductionHandler.addObserver(DeductionTypeEnum.GOLD.getType(), PlayerModel.getInstance());

      // 观察者模式，添加货物派发事件观察者，用于道具等物品的派发
      DeliveryHandler.addObserver(AuctionTypeEnum.Item.getType(), ItemModel.getInstance());
    }, res -> {
      // 预加载错误就无法启动服务器
      if(res.failed()){
        res.cause().printStackTrace();
        System.exit(-1);
      }
    });

    // 监听连接
    NetServer server = vertx.createNetServer();
    server.connectHandler(handler -> {
      String hId = handler.writeHandlerID();
      clientModel.addClient(hId,handler);
      welCome(hId);
      StringBuilder sb = new StringBuilder();
      handler.handler(buff -> {
        // 监控客户端按下回车键
        if(buff.toString().equals("\r\n")){
          vertx.executeBlocking(promise -> {
            // 进入 worker 线程
            List<CmdMessageResult> list = cmdModel.execute(hId, sb.toString());
            for(CmdMessageResult msg : list){
              messageOuter.out(msg.getMessage(), msg.gethId());
            }
            sb.delete(0, sb.length());
          }, res -> {
            if(res.failed()){
              res.cause().printStackTrace();
            }
          });
        }else{
          sb.append(buff.toString());
        }
      });

      handler.closeHandler(fun -> {
        vertx.executeBlocking(promise -> {
          Jedis jedis = JedisPool.getJedis();
          ThreadLocalJedisUtils.addJedis(jedis);
          try{
            ClientCloseHandler.notify(hId);
          }finally {
            JedisPool.returnJedis(jedis);
          }
        });
      });
    });

    // 启动服务
    server.listen(2233, "localhost");
  }

  private void welCome(String hId){
    StringBuilder sb = new StringBuilder();
    sb.append("============================================================\r\n")
            .append("欢迎进入,您可以使用以下命令:\r\n")
            .append("============================================================\r\n")
            .append("[login:username:password]:登录\r\n")
            .append("[register:username:password]:注册\r\n")
            .append("============================================================");
    messageOuter.out(sb.toString(), hId);
  }
}
