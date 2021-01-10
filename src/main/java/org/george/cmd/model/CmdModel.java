package org.george.cmd.model;

import org.george.cmd.config.bean.CmdDescConfigBean;
import org.george.cmd.model.pojo.CmdMessageResult;
import org.george.cmd.model.impl.CmdModelImpl;

import java.util.List;

public interface CmdModel {

    List<CmdMessageResult> execute(String hId, String message);

    List<CmdDescConfigBean> getCmdDescriptions();

    static CmdModel getInstance(){
        return CmdModelImpl.getInstance();
    }
}
