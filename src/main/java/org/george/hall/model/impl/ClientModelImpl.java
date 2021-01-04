package org.george.hall.model.impl;

import io.vertx.core.net.NetSocket;
import org.george.hall.model.ClientModel;

import java.util.HashMap;
import java.util.Map;

public class ClientModelImpl implements ClientModel {

  Map<String, NetSocket> clientMap = new HashMap<>();

  Map<String, String> userIdHIdMap = new HashMap<>();

  Map<String, String> hIdUserIdMap = new HashMap<>();

  private ClientModelImpl(){}

  private static ClientModelImpl client = new ClientModelImpl();

  public static ClientModelImpl getInstance(){
    return client;
  }

  @Override
  public void clientClosed(String hId) {
    NetSocket netSocket = clientMap.get(hId);
    if(netSocket != null)
      netSocket.close();
    clientMap.remove(hId);
  }

  @Override
  public String getUserIdByHId(String hId) {
    return hIdUserIdMap.get(hId);
  }

  @Override
  public String getHIdByUserId(String userId) {
    return userIdHIdMap.get(userId);
  }

  @Override
  public void addUserIdHId(String userId, String hId) {
    hIdUserIdMap.put(hId, userId);
    userIdHIdMap.put(userId, hId);
  }

  @Override
  public void logout(String userId) {
    String hId = userIdHIdMap.get(userId);
    if(hId != null){
      hIdUserIdMap.remove(hId);
    }
    userIdHIdMap.remove(userId);
  }

  @Override
  public void addClient(String HId, NetSocket socket){
    clientMap.put(HId, socket);
  }


  @Override
  public void update(String hId) {
    clientClosed(hId);
    String userId = getUserIdByHId(hId);
    if(userId != null){
      logout(userId);
    }
  }
}
