package com.np.rift.main.personalFragment.addExp;


import java.util.Date;

public class MonthModel {
    private String name;
    private int monthNumber;
    private String expense;
    private Date date;


    public int getMonthNumber() {
        return monthNumber;
    }

    public void setMonthNumber(int monthNumber) {
        this.monthNumber = monthNumber;
    }

    public String getExpense() {
        return expense;
    }

    public void setExpense(String expense) {
        this.expense = expense;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    @Override
    public String toString() {
        return "MonthModel{" +
                "name='" + name + '\'' +
                ", monthNumber=" + monthNumber +
                ", expense='" + expense + '\'' +
                '}';
    }
}
