package org.george.auction.dao.impl;

import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import org.george.auction.dao.AuctionDao;
import org.george.auction.dao.bean.AuctionBean;
import org.george.util.JFinalUtils;

import java.util.ArrayList;
import java.util.List;

public class AuctionDaoImpl implements AuctionDao {

    private static AuctionDaoImpl instance = new AuctionDaoImpl();

    private AuctionDaoImpl(){}

    public static AuctionDaoImpl getInstance(){
        return instance;
    }

    @Override
    public List<AuctionBean> getAuctions() {
        List<Object[]> list = Db.query("select auction_id, auction_type, deduction_type, cost, num from auction");
        List<AuctionBean> beans = new ArrayList<>();
        for(Object[] o : list){
            AuctionBean bean = new AuctionBean();
            bean.setAuctionId((Integer) o[0]);
            bean.setAuctionType((Integer) o[1]);
            bean.setDeductionType((Integer) o[2]);
            bean.setCost((Integer) o[3]);
            bean.setNum((Integer) o[4]);
            beans.add(bean);
        }
        return beans;
    }

    @Override
    public void addAuctionItem(Integer itemId, Integer num) {
        Record record = new Record();
        record.set("auction_id", itemId).set("num", num);
        Db.save("auction", "auction_id", record);
    }

    @Override
    public void updateSelective(AuctionBean bean) {

        Record record = new Record();
        record.set("auction_id", bean.getAuctionId());
        if(bean.getNum() != null){
            record.set("num", bean.getNum());
        }
        if(bean.getCost() != null){
            record.set("cost", bean.getCost());
        }
        if(bean.getAuctionType() != null){
            record.set("auction_type", bean.getAuctionType());
        }
        if(bean.getDeductionType() != null){
            record.set("deduction_type", bean.getAuctionType());
        }
        Db.update("auction", "auction_id", record);
    }

    @Override
    public void deleteAuctionItem(Integer auctionId) {
        Db.delete("delete from auction where auction_id = ?", auctionId);
    }

    @Override
    public AuctionBean getAuction(Integer auctionId) {
        AuctionBean bean = null;
        Record record = Db.findFirst("select * from auction where auction_id = ?", auctionId);
        if(record != null){
            bean = new AuctionBean();
            bean.setAuctionId(auctionId);
            bean.setAuctionType(record.getInt("auction_type"));
            bean.setDeductionType(record.getInt("deduction_type"));
            bean.setCost(record.getInt("cost"));
            bean.setNum(record.getInt("num"));
        }
        return bean;
    }

    public static void main(String[] args) {
        JFinalUtils.initJFinalConfig();
        AuctionDaoImpl auctionDao = new AuctionDaoImpl();
        System.out.println(auctionDao.getAuctions());
    }
}
