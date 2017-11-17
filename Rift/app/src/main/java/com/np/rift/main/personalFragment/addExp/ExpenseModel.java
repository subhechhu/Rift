package com.np.rift.main.personalFragment.addExp;


import java.util.Date;

public class ExpenseModel {
    //    String name, price, description, productId; //to be deleted
    String id, date, spentOn, amount, type, userId;
    Boolean selected;
    Date realDate;


    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Boolean getSelected() {
        return selected;
    }

    public void setSelected(Boolean selected) {
        this.selected = selected;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getSpentOn() {
        return spentOn;
    }

    public void setSpentOn(String spentOn) {
        this.spentOn = spentOn;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public Date getRealDate() {
        return realDate;
    }

    public void setRealDate(Date realDate) {
        this.realDate = realDate;
    }

    @Override
    public String toString() {
        return "ExpenseModel{" +
                "id='" + id + '\'' +
                ", date='" + date + '\'' +
                ", spentOn='" + spentOn + '\'' +
                ", amount='" + amount + '\'' +
                ", type='" + type + '\'' +
                ", userId='" + userId + '\'' +
                ", selected=" + selected +
                ", realDate=" + realDate +
                '}';
    }
}
