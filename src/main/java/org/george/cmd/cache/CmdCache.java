package org.george.cmd.cache;

import org.george.cmd.cache.impl.CmdCacheImpl;

import java.lang.reflect.Method;

public interface CmdCache {

    void addCmdClassObj(String cmd, Object obj);

    Object getCmdClassObj(String cmd);

    void addCmdMethod(String cmd, Method method);

    Method getCmdMethod(String cmd);

    static CmdCache getInstance(){
        return CmdCacheImpl.getInstance();
    }
}
