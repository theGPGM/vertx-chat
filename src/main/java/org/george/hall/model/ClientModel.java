package org.george.hall.model;

import io.vertx.core.net.NetSocket;
import org.george.hall.ClientCloseEventObserver;
import org.george.hall.model.impl.ClientModelImpl;

public interface ClientModel extends ClientCloseEventObserver {

    void addClient(String hId, NetSocket socket);

    void clientClosed(String hId);

    String getUserIdByHId(String hId);

    String getHIdByUserId(String userId);

    void addUserIdHId(String userId, String hId);

    void logout(String userId);

    static ClientModel getInstance(){
        return ClientModelImpl.getInstance();
    }
}
