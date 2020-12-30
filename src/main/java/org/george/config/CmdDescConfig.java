package org.george.config;

import org.george.config.bean.CmdDescConfigBean;
import org.george.config.impl.CmdDescConfigImpl;

import java.util.List;

public interface CmdDescConfig {

    static CmdDescConfig getInstance(){
        return CmdDescConfigImpl.getInstance();
    }

    void addCmdDescription(String cmd, String description);

    CmdDescConfigBean getCmdDescriptionBean(String cmd);

    List<CmdDescConfigBean> getAllCmdDescriptionBeans();
}
