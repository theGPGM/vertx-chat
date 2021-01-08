package org.george.config.impl;

import org.george.config.ItemConfig;
import org.george.config.bean.ItemInfoBean;
import org.george.pojo.CSVFormatException;
import org.george.util.NumUtils;

import java.io.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ItemConfigImpl implements ItemConfig {

    private ItemConfigImpl(){}

    private static ItemConfigImpl instance = new ItemConfigImpl();

    public static ItemConfigImpl getInstance(){
        return instance;
    }

    Set<Integer> itemIdSet = new HashSet<>();
    
    Set<String> itemNameSet = new HashSet<>();

    Map<Integer, ItemInfoBean> map = new HashMap<>();
    
    @Override
    public void loadItemInfo(String fileName) {

        File file = new File(fileName);
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
                ItemInfoBean bean = new ItemInfoBean();

                if(properties.length < 3){
                    throw new CSVFormatException("csv 文件第[" + count + "]行编写错误，属性缺失");
                }

                for(String pro : properties){
                    if(pro == null || pro.length() == 0){
                        throw new CSVFormatException("item.csv 文件第[" + count + "]行编写错误，属性缺失");
                    }
                }

                if(!NumUtils.checkDigit(properties[0])){
                    throw new CSVFormatException("item.csv 文件第[" + count + "]行编写错误，使用非数字");
                }{

                    Integer itemId = Integer.parseInt(properties[0]);
                    String itemName = properties[1];
                    String description = properties[2];
                    
                    if(!itemIdSet.add(itemId)){
                        throw new CSVFormatException("item.csv 文件第[" + count + "]行编写错误，道具 ID 重复");
                    }

                    if(!itemNameSet.add(itemName)){
                        throw new CSVFormatException("item.csv 文件第[" + count + "]行编写错误，道具名称重复");
                    }

                    bean.setItemId(itemId);
                    bean.setItemName(itemName);
                    bean.setDescription(description);

                    map.put(itemId, bean);
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
    public ItemInfoBean getItemInfoBean(Integer itemId) {
        return map.get(itemId);
    }
}
