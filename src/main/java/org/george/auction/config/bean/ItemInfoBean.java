package org.george.auction.config.bean;

public class ItemInfoBean {

    private Integer itemId;

    private String itemName;

    private String description;

    public Integer getItemId() {
        return itemId;
    }

    public void setItemId(Integer itemId) {
        this.itemId = itemId;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "ItemInfoBean{" +
                "itemId=" + itemId +
                ", itemName='" + itemName + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}
