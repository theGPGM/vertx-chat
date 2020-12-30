package org.george.util;

import org.apache.ibatis.session.SqlSession;

public class ThreadLocalSessionUtils {

    private static final ThreadLocal<SqlSession> threadLocal = new ThreadLocal<>();

    public static SqlSession getSession(){
        return threadLocal.get();
    }

    public static void addSqlSession(SqlSession session){
        threadLocal.set(session);
    }
}
