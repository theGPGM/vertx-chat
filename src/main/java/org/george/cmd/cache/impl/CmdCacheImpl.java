package org.george.cmd.cache.impl;

import org.george.cmd.cache.CmdCache;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CmdCacheImpl implements CmdCache {

    private static CmdCacheImpl instance = new CmdCacheImpl();

    private CmdCacheImpl(){};

    private Map<String, Object> cmdClassObjMap = new HashMap<>();

    private Map<String, Method> cmdMethodMap = new HashMap<>();

    private List<String> commandDescriptions = new ArrayList<>();

    public void addCmdClassObj(String cmd, Object obj){
        cmdClassObjMap.put(cmd, obj);
    }

    public Object getCmdClassObj(String cmd){
        return cmdClassObjMap.get(cmd);
    }

    public void addCmdMethod(String cmd, Method method){
        cmdMethodMap.put(cmd, method);
    }

    public Method getCmdMethod(String cmd){
        return cmdMethodMap.get(cmd);
    }

    public static CmdCacheImpl getInstance(){
        return instance;
    }
}
