package org.george.cmd.config.impl;


import org.george.cmd.cache.CmdCache;
import org.george.cmd.config.CmdDescConfig;
import org.george.cmd.config.bean.CmdDescConfigBean;
import org.george.cmd.util.PropertiesUtils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class CmdDescConfigImpl implements CmdDescConfig {

    private static CmdDescConfigImpl instance = new CmdDescConfigImpl();

    private CmdDescConfigImpl(){}

    public static CmdDescConfigImpl getInstance(){
        return instance;
    }

    private static CmdCache cmdCache = CmdCache.getInstance();

    private final static List<CmdDescConfigBean> list = new ArrayList<>();

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

    static{
        Properties descPros = PropertiesUtils.loadProperties("src/main/resources/conf/description.properties");
        Properties cmdPros = PropertiesUtils.loadProperties("src/main/resources/conf/cmds.properties");
        for(String cmd : cmdPros.stringPropertyNames()){
            String cmdClazz = cmdPros.getProperty(cmd);
            try {
                String[] split = cmdClazz.split("\\.");
                String m = split[split.length - 1];

                StringBuilder sb = new StringBuilder();
                for(int i = 0; i < split.length - 1; i++){
                    if(i == split.length - 2){
                        sb.append(split[i]);
                    }else{
                        sb.append(split[i]);
                        sb.append(".");
                    }
                }

                Method method = Class.forName(sb.toString()).getDeclaredMethod(m, String[].class);
                cmdCache.addCmdClassObj(cmd, Class.forName(sb.toString()).newInstance());
                cmdCache.addCmdMethod(cmd, method);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            }

            for(String key : descPros.stringPropertyNames()){
                CmdDescConfigBean bean = new CmdDescConfigBean();
                bean.setCmd(key);
                bean.setDesc(descPros.getProperty(key));
                list.add(bean);
            }
        }
    }
}
