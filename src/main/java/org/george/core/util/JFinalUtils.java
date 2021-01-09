package org.george.core.util;

import com.jfinal.plugin.activerecord.ActiveRecordPlugin;
import com.jfinal.plugin.druid.DruidPlugin;

public class JFinalUtils {

    public static void initJFinalConfig(){

        DruidPlugin dp = new DruidPlugin("jdbc:mysql://localhost:3306/dungeon_game?serverTimezone=Asia/Shanghai", "root", "123456");
        ActiveRecordPlugin arp = new ActiveRecordPlugin(dp);
        dp.start();
        arp.start();
    }
}
