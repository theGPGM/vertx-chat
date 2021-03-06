package org.george.item.config.impl;

import org.george.item.config.ItemConfig;
import org.george.item.config.bean.ItemInfoBean;
import org.george.item.pojo.CSVFormatException;
import org.george.item.uitl.NumUtils;

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

    private static Set<Integer> itemIdSet = new HashSet<>();
    
    private static Set<String> itemNameSet = new HashSet<>();

    private static Map<Integer, ItemInfoBean> map = new HashMap<>();
    
    static {

        File file = new File("src/main/java/org/george/item/config/csv/item.csv");
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
