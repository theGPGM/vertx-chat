package org.george.cmd.model;

//import org.george.cmd.model.impl.CmdModelImpl;
import org.george.common.pojo.Message;

import java.util.List;
import java.util.Properties;

public interface CmdModel {

    void loadCmdProperties(Properties cmdProperties);

    void loadCmdDescriptionProperties(Properties cmdDescriptionProperties);

    List<Message> execute(String message);

    static CmdModel getInstance(){
        return null;
    }
}
