package org.george.config.bean;

import org.george.pojo.LevelBean;
import org.george.pojo.Monster;
import org.george.config.LevelInfoConfig;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class LevelInfoConfigImpl implements LevelInfoConfig {

    List<LevelBean> levelBeans = new ArrayList<>();

    private LevelInfoConfigImpl(){}

    private static LevelInfoConfigImpl instance = new LevelInfoConfigImpl();

    public static LevelInfoConfigImpl getInstance(){
        return instance;
    }

    @Override
    public void loadLevelInfo(String filename) {
        BufferedReader br = null;
        List<LevelBean> list = new ArrayList<>();
        File file = new File(filename);
        if(file == null){
            throw new RuntimeException("文件为空");
        }else{
            try {
                br = new BufferedReader(new FileReader(file));
                // 第一行不读取
                br.readLine();
                String line = null;
                while((line = br.readLine()) != null){
                    String[] properties = line.split(",");
                    LevelBean levelBean = new LevelBean();


                    String property = properties[0];
                    levelBean.setLevel(Integer.parseInt(property));
                    levelBean.setLevelName(properties[1]);
                    Monster monster = new Monster();
                    monster.setMonsterId(Integer.parseInt(properties[2]));
                    monster.setMonsterName(properties[3]);
                    levelBean.setMonster(monster);
                    levelBean.setWinningRate(Integer.parseInt(properties[4]));
                    levelBean.setDroppingWinItemRate(Integer.parseInt(properties[5]));
                    list.add(levelBean);
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public LevelBean getLevelInfo(Integer level) {
        return levelBeans.get(level);
    }

    @Override
    public Integer getLevelNum() {
        return levelBeans.size();
    }
}
