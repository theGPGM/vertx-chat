package org.george.config.impl;

import org.george.config.LevelInfoConfig;
import org.george.config.bean.LevelBean;
import org.george.config.bean.Monster;
import org.george.pojo.CSVFormatException;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LevelInfoConfigImpl implements LevelInfoConfig {

    Map<Integer, LevelBean> levelBeanMap = new HashMap<>();

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
                int count = 1;
                while((line = br.readLine()) != null){
                    String[] properties = line.split(",");
                    LevelBean levelBean = new LevelBean();

                    for(String pro : properties){
                        if(pro == null){
                            throw new CSVFormatException("csv 文件第" + count + "行编写错误");
                        }
                    }

                    if(!checkDigit(properties[0]) || !checkDigit(properties[2]) || !checkDigit(properties[2]) || !checkDigit(properties[2])){
                        throw new CSVFormatException("csv 文件第" + count + "行编写错误");
                    }{

                        Integer levelId = Integer.parseInt(properties[0]);
                        String levelName = properties[1];
                        Integer monsterId = Integer.parseInt(properties[2]);
                        String monsterName = properties[3];
                        Integer winningRate = Integer.parseInt(properties[4]);
                        Integer droppingWinItemRate = Integer.parseInt(properties[5]);

                        if(winningRate > 100 || winningRate < 0){
                            throw new CSVFormatException("csv 文件第" + count + "行编写错误, 胜率编写错误");
                        }else if(droppingWinItemRate > 100 || droppingWinItemRate < 0){
                            throw new CSVFormatException("csv 文件第" + count + "行编写错误, 道具掉落率编写错误");
                        }else{
                            levelBean.setLevelId(levelId);
                            levelBean.setLevelName(levelName);
                            Monster monster = new Monster();
                            monster.setMonsterId(monsterId);
                            monster.setMonsterName(monsterName);
                            levelBean.setMonster(monster);
                            levelBean.setWinningRate(winningRate);
                            levelBean.setDroppingWinItemRate(droppingWinItemRate);

                            levelBeanMap.put(levelId, levelBean);
                        }
                    }
                    count++;
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public LevelBean getLevelBean(Integer levelId) {
        return levelBeanMap.get(levelId);
    }

    @Override
    public Integer getLevelNum() {
        return levelBeanMap.size();
    }

    private boolean checkDigit(String roomId){
        char[] arr = roomId.toCharArray();
        for(char c : arr){
            if(c > '9' || c < '0') return false;
        }
        return true;
    }
}
