package com.np.rift.main.addExp;

public class AddItemModel {
    String productId, productName, productPrice, date;
    Boolean selected;

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getProductPrice() {
        return productPrice;
    }

    public void setProductPrice(String productPrice) {
        this.productPrice = productPrice;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public Boolean getSelected() {
        return selected;
    }

    public void setSelected(Boolean selected) {
        this.selected = selected;
    }

    @Override
    public String toString() {
        return "AddItemModel{" +
                "productId='" + productId + '\'' +
                ", productName='" + productName + '\'' +
                ", productPrice='" + productPrice + '\'' +
                ", date='" + date + '\'' +
                '}';
    }
}
