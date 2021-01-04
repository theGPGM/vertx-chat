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

    private final List<CmdDescConfigBean> list = new ArrayList<>();
    @Override
    public void addCmdDescription(String cmd, String description) {
        CmdDescConfigBean bean = new CmdDescConfigBean();
        bean.setCmd(cmd);
        bean.setDesc(description);
        list.add(bean);
    }

    @Override
    public CmdDescConfigBean getCmdDescriptionBean(String cmd) {
        for(CmdDescConfigBean bean : list){
            if(bean.getCmd().equals(cmd)){
                return bean;
            }
        }
        return null;
    }

    @Override
    public List<CmdDescConfigBean> getAllCmdDescriptionBeans() {
        return list;
    }
}
