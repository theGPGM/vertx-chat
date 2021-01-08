package org.george.hall.model.impl;

import io.vertx.core.net.NetSocket;
import org.george.hall.model.ClientModel;

import java.util.HashMap;
import java.util.Map;

public class ClientModelImpl implements ClientModel {

  Map<String, NetSocket> clientMap = new HashMap<>();

  private ClientModelImpl(){}

  private static ClientModelImpl client = new ClientModelImpl();

  public static ClientModelImpl getInstance(){
    return client;
  }

  @Override
  public void close(String hId) {
    NetSocket netSocket = clientMap.get(hId);
    if(netSocket != null)
      netSocket.close();
    clientMap.remove(hId);
  }

  @Override
  public void addClient(String HId, NetSocket socket){
    clientMap.put(HId, socket);
  }

  @Override
  public void clientCloseNotify(String hId) {
    clientMap.remove(hId);
  }
}
