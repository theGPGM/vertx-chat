package org.george.util;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.IOException;
import java.io.InputStream;

public class SessionPool {

    private static SqlSessionFactory factory = null;

    static {
        // 1 读取配置文件 config.xml
        InputStream in = null;
        try {
            in = Resources.getResourceAsStream("mybatis-config.xml");
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        // 2 创建SqlSessionFactory
        try{
            factory = new SqlSessionFactoryBuilder().build(in);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static SqlSession openSession() {
        return factory.openSession();
    }

    public static void close(SqlSession sqlSession) {
        if (sqlSession != null) {
            sqlSession.close();
        }
    }
}
