package org.george.item.dao.bean;

/**
 * 道具
 */
public class ItemBean {

    private Integer itemId;

    private String ItemName;

    private String description;

    public Integer getItemId() {
        return itemId;
    }

    public void setItemId(Integer itemId) {
        this.itemId = itemId;
    }

    public String getItemName() {
        return ItemName;
    }

    public void setItemName(String itemName) {
        ItemName = itemName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "Item{" +
                "itemId=" + itemId +
                ", ItemName='" + ItemName + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}
