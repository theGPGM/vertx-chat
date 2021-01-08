package org.george.config.impl;

import org.george.config.AuctionConfig;
import org.george.config.ItemConfig;
import org.george.config.bean.AuctionInfoBean;
import org.george.pojo.AuctionTypeEnum;
import org.george.pojo.CSVFormatException;
import org.george.pojo.DeductionTypeEnum;
import org.george.util.NumUtils;

import java.io.*;
import java.util.*;

public class AuctionConfigImpl implements AuctionConfig {

    private AuctionConfigImpl(){}

    private static AuctionConfigImpl instance = new AuctionConfigImpl();

    public static AuctionConfigImpl getInstance(){
        return instance;
    }

    private Map<Integer, AuctionInfoBean> map = new HashMap<>();

    private ItemConfig itemConfig = ItemConfig.getInstance();
    
    @Override
    public List<AuctionInfoBean> getAllAuctionInfo() {
        return new ArrayList<>(map.values());
    }

    @Override
    public AuctionInfoBean getAuctionInfo(Integer id) {
        return map.get(id);
    }

    @Override
    public void loadAuctionInfo(String filename) {
    	File file = new File(filename);
    	if(file == null || !file.isFile()) {
    		throw new CSVFormatException("文件错误");
    	}
        try (BufferedReader br  = new BufferedReader(new FileReader(file))){
		    
        	// 第一行不读取
		    br.readLine();
		    String line = null;
		    int count = 2;
		    while((line = br.readLine()) != null){
		        String[] properties = line.split(",");
		        AuctionInfoBean bean = new AuctionInfoBean();

				if(properties.length < 5){
					throw new CSVFormatException("auction.csv 文件第[" + count + "]行编写错误，缺少属性");
				}

				for(String pro : properties){
					if(pro == null || pro.length() == 0){
						throw new CSVFormatException("auction.csv 文件第[" + count + "]行编写错误，缺少属性");
					}
				}

		        if(!NumUtils.checkDigit(properties[0]) || !NumUtils.checkDigit(properties[1]) || !NumUtils.checkDigit(properties[2]) || !NumUtils.checkDigit(properties[3]) || !NumUtils.checkDigit(properties[4]) ){
		            throw new CSVFormatException("auction.csv 文件第[" + count + "]行编写错误，非正整数");
		        }{

		            Integer auctionId = Integer.parseInt(properties[0]);
		            Integer auctionType = Integer.parseInt(properties[1]);
		            Integer deductionType = Integer.parseInt(properties[2]);
		            Integer cost = Integer.parseInt(properties[3]);
		            Integer num = Integer.parseInt(properties[4]);

		            if(!containsAuctionType(auctionType)){
		                throw new CSVFormatException("auction.csv 文件第[" + count + "]行编写错误，物品类型未知");
		            }else if(!containsDeductionType(deductionType)) {
		                throw new CSVFormatException("auction.csv 文件第[" + count + "]行编写错误，货币类型未知");
		            }else if(cost < 0){
		                throw new CSVFormatException("auction.csv 文件第[" + count + "]行编写错误，售价为负数");
		            }else if(num < 0){
		                throw new CSVFormatException("auction.csv 文件第[" + count + "]行编写错误，数量为负数");
		            } else{
		            	
		            	// 道具
		            	if(auctionType.equals(AuctionTypeEnum.Item.getType())){
		            		// 检查是否存在该道具
							if(itemConfig.getItemInfoBean(auctionId) == null){
								throw new CSVFormatException("auction.csv 文件第[" + count + "]行编写错误，该道具 ID 不存在");
							}
						}
		            	
		                bean.setAuctionId(auctionId);
		                bean.setAuctionType(auctionType);
		                bean.setDeductionType(deductionType);
		                bean.setCost(cost);
		                bean.setNum(num);
		                map.put(auctionId, bean);
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

    private boolean containsAuctionType(Integer auctionType){
        for(AuctionTypeEnum type : AuctionTypeEnum.values()){
            if(type.getType() == auctionType){
                return true;
            }
        }
        return false;
    }

    private boolean containsDeductionType(Integer deductionType){
        for(DeductionTypeEnum type : DeductionTypeEnum.values()){
            if(type.getType() == deductionType){
                return true;
            }
        }
        return false;
    }
}
