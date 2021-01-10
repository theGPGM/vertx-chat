package org.george.dungeon_game.config.impl;


import org.george.dungeon_game.config.LevelInfoConfig;
import org.george.dungeon_game.config.bean.LevelBean;
import org.george.dungeon_game.config.bean.MonsterBean;
import org.george.dungeon_game.pojo.CSVFormatException;
import org.george.dungeon_game.util.NumUtils;

import java.io.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class LevelInfoConfigImpl implements LevelInfoConfig {

    private static Map<Integer, LevelBean> levelBeanMap = new HashMap<>();

    private static Set<Integer> monsterIdSet = new HashSet<>();

    private static Set<Integer> levelSet = new HashSet<>();

    private static Set<String> levelNameSet = new HashSet<>();

    private static Set<String> monsterNameSet = new HashSet<>();

    private LevelInfoConfigImpl(){}

    private static LevelInfoConfigImpl instance = new LevelInfoConfigImpl();

    public static LevelInfoConfigImpl getInstance(){
        return instance;
    }

    static {
        File file = new File("src/main/java/org/george/dungeon_game/config/csv/level.csv");
        if(file == null || !file.isFile()){
            throw new CSVFormatException("文件错误");
        }
        
        try(BufferedReader br = new BufferedReader(new FileReader(file));) {
            
            // 第一行不读取
            br.readLine();
            String line = null;
            int count = 2;
            while((line = br.readLine()) != null){
                String[] properties = line.split(",");
                LevelBean levelBean = new LevelBean();

                if(properties.length < 5){
                    throw new CSVFormatException("level.csv 文件第[" + count + "]行编写错误, 属性缺失");
                }
                
                for(String pro : properties){
                    if(pro == null || pro.length() == 0){
                        throw new CSVFormatException("level.csv 文件第[" + count + "]行编写错误");
                    }
                }

                if(!NumUtils.checkDigit(properties[0]) || !NumUtils.checkDigit(properties[2]) || !NumUtils.checkDigit(properties[4])){
                    throw new CSVFormatException("level.csv 文件第[" + count + "]行编写错误，使用非数字字符");
                }{

                    Integer levelId = Integer.parseInt(properties[0]);
                    String levelName = properties[1];
                    Integer monsterId = Integer.parseInt(properties[2]);
                    String monsterName = properties[3];
                    Integer winningRate = Integer.parseInt(properties[4]);

                    if(!levelSet.add(levelId)){
                        throw new CSVFormatException("level.csv 文件第[" + count + "]行编写错误, 关卡 ID 重复");
                    }

                    if(!monsterIdSet.add(monsterId)){
                        throw new CSVFormatException("level.csv 文件第[" + count + "]行编写错误, 关卡怪物 ID 重复");
                    }

                    if(!levelNameSet.add(levelName)){
                        throw new CSVFormatException("level.csv 文件第[" + count + "]行编写错误, 关卡名称重复");
                    }

                    if(!monsterNameSet.add(monsterName)){
                        throw new CSVFormatException("level.csv 文件第[" + count + "]行编写错误, 关卡怪物名称重复");
                    }

                    if(winningRate > 100 || winningRate < 0){
                        throw new CSVFormatException("level.csv 文件第[" + count + "]行编写错误, 胜率编写错误");
                    }else{
                        levelBean.setLevelId(levelId);
                        levelBean.setLevelName(levelName);
                        MonsterBean monsterBean = new MonsterBean();
                        monsterBean.setMonsterId(monsterId);
                        monsterBean.setMonsterName(monsterName);
                        levelBean.setMonsterBean(monsterBean);
                        levelBean.setWinningRate(winningRate);

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

    @Override
    public LevelBean getLevelBean(Integer levelId) {
        return levelBeanMap.get(levelId);
    }

    @Override
    public Integer getLevelNum() {
        return levelBeanMap.size();
    }
}
