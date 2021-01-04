package org.george.cmd.model;

//import org.george.cmd.model.impl.CmdModelImpl;
import org.george.cmd.model.bean.CmdMessageResult;
import org.george.common.pojo.Message;
import org.george.common.pojo.Messages;

import java.util.List;
import java.util.Properties;

public interface CmdModel {

    void loadCmdProperties(Properties cmdProperties);

    void loadCmdDescriptionProperties(Properties cmdDescriptionProperties);

    List<CmdMessageResult> execute(String hId, String message);

    static CmdModel getInstance(){
        return null;
    }
}
