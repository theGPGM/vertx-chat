package org.george.auction.config.impl;

import org.george.auction.config.AuctionConfig;
import org.george.auction.config.ItemConfig;
import org.george.auction.config.bean.AuctionInfoBean;
import org.george.auction.pojo.AuctionTypeEnum;
import org.george.auction.pojo.CSVFormatException;
import org.george.auction.pojo.DeductionTypeEnum;
import org.george.auction.util.NumUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AuctionConfigImpl implements AuctionConfig {

    private AuctionConfigImpl(){}

    private static AuctionConfigImpl instance = new AuctionConfigImpl();

    public static AuctionConfigImpl getInstance(){
        return instance;
    }

    private final static Map<Integer, AuctionInfoBean> map = new HashMap<>();

    private final static ItemConfig itemConfig = ItemConfig.getInstance();
    
    @Override
    public List<AuctionInfoBean> getAllAuctionInfo() {
        return new ArrayList<>(map.values());
    }

    @Override
    public AuctionInfoBean getAuctionInfo(Integer id) {
        return map.get(id);
    }

    static {

    	File file = new File("src/main/java/org/george/auction/config/csv/auction.csv");
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

    private static boolean containsAuctionType(Integer auctionType){
        for(AuctionTypeEnum type : AuctionTypeEnum.values()){
            if(type.getType() == auctionType){
                return true;
            }
        }
        return false;
    }

    private static boolean containsDeductionType(Integer deductionType){
        for(DeductionTypeEnum type : DeductionTypeEnum.values()){
            if(type.getType() == deductionType){
                return true;
            }
        }
        return false;
    }
}
