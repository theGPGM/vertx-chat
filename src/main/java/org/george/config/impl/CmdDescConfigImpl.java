package org.george.config.impl;

import org.george.config.CmdDescConfig;
import org.george.config.bean.CmdDescConfigBean;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CmdDescConfigImpl implements CmdDescConfig {

    private static CmdDescConfigImpl instance = new CmdDescConfigImpl();

    private CmdDescConfigImpl(){}

    public static CmdDescConfigImpl getInstance(){
        return instance;
    }

    private final Map<String, CmdDescConfigBean> map = new HashMap<>();

    @Override
    public void addCmdDescription(String cmd, String description) {
        CmdDescConfigBean bean = new CmdDescConfigBean();
        bean.setCmd(cmd);
        bean.setDesc(description);
    }

    @Override
    public CmdDescConfigBean getCmdDescriptionBean(String cmd) {
        return map.get(cmd);
    }

    @Override
    public List<CmdDescConfigBean> getAllCmdDescriptionBeans() {
        return new ArrayList<>(map.values());
    }
}
