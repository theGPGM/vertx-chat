package org.george.dungeon_game.config.impl;

import org.george.dungeon_game.config.DropItemConfig;
import org.george.dungeon_game.config.ItemConfig;
import org.george.dungeon_game.config.bean.DropItemInfoBean;
import org.george.dungeon_game.pojo.CSVFormatException;
import org.george.dungeon_game.util.NumUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DropItemConfigImpl implements DropItemConfig {

    private DropItemConfigImpl(){}

    public static DropItemConfigImpl getInstance(){
        return instance;
    }

	private static Map<Integer, List<DropItemInfoBean>> map = new HashMap<>();

	private static DropItemConfigImpl instance = new DropItemConfigImpl();

    private static final ItemConfig itemConfig = ItemConfig.getInstance();

    static {
        File file = new File("src/main/java/org/george/dungeon_game/config/csv/drop_item.csv");
        if(file == null || !file.isFile()){
            throw new CSVFormatException("文件错误");
        }
		
        try (BufferedReader br = new BufferedReader(new FileReader(file))){
		       
		        // 第一行不读取
		        br.readLine();
		        String line = null;
		        int count = 2;
		        while((line = br.readLine()) != null){
		            String[] properties = line.split(",");
		            DropItemInfoBean bean = new DropItemInfoBean();

					if(properties.length < 3){
						throw new CSVFormatException("drop_item.csv 文件第" + count + "行编写错误");
					}

					for(String pro : properties){
						if(pro == null || pro.length() == 0){
							throw new CSVFormatException("drop_item.csv 文件第[" + count + "]行编写错误，属性为空");
						}
					}

		            if(!NumUtils.checkDigit(properties[0]) || !NumUtils.checkDigit(properties[1]) || !NumUtils.checkDigit(properties[2])){
		                throw new CSVFormatException("drop_item.csv 文件第[" + count + "]行编写错误，使用非数字字符");
		            }{
		
		                Integer level = Integer.parseInt(properties[0]);
		                Integer itemId = Integer.parseInt(properties[1]);
		                Integer rate = Integer.parseInt(properties[2]);
		
		                if(rate > 100 || rate < 0){
		                    throw new CSVFormatException("drop_item.csv 文件第[" + count + "]行编写错误, 掉落率编写错误");
		                }else if(itemConfig.getItemInfoBean(itemId) == null){
							throw new CSVFormatException("drop_item.csv 文件第[" + count + "]行编写错误, 该道具 ID 不存在");
						} else{
		                    bean.setItemId(itemId);
		                    bean.setLevel(level);
		                    bean.setRate(rate);
		
		                    if(map.get(level) == null){
		                        List<DropItemInfoBean> list = new ArrayList<>();
		                        list.add(bean);
		                        map.put(level, list);
		                    }else{
		                        map.get(level).add(bean);
		                    }
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
    public List<DropItemInfoBean> getLevelDropItemInfo(Integer level) {
        return map.get(level);
    }


}
