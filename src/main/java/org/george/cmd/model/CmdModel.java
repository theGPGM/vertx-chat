package org.george.cmd.model;

import org.george.cmd.model.pojo.CmdMessageResult;
import org.george.cmd.model.impl.CmdModelImpl;
import org.george.config.bean.CmdDescConfigBean;

import java.util.List;
import java.util.Properties;

public interface CmdModel {

    void loadCmdProperties(Properties cmdProperties);

    void loadCmdDescriptionProperties(Properties cmdDescriptionProperties);

    List<CmdMessageResult> execute(String hId, String message);

    List<CmdDescConfigBean> getCmdDescriptions();

    static CmdModel getInstance(){
        return CmdModelImpl.getInstance();
    }
}
