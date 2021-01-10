package org.george.cmd.config;


import org.george.cmd.config.bean.CmdDescConfigBean;
import org.george.cmd.config.impl.CmdDescConfigImpl;

import java.util.List;

public interface CmdDescConfig {

    static CmdDescConfig getInstance(){
        return CmdDescConfigImpl.getInstance();
    }

    CmdDescConfigBean getCmdDescriptionBean(String cmd);

    List<CmdDescConfigBean> getAllCmdDescriptionBeans();
}
