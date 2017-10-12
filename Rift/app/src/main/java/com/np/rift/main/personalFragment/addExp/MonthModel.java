package com.np.rift.main.personalFragment.addExp;


import java.util.Comparator;
import java.util.Date;
import java.util.Map;

public class MonthModel {
    private String name;
    private int monthNumber;
    private String monthExpense;
    private Date monthDate;


    public int getMonthNumber() {
        return monthNumber;
    }

    public void setMonthNumber(int monthNumber) {
        this.monthNumber = monthNumber;
    }

    public String getMonthExpense() {
        return monthExpense;
    }

    public void setMonthExpense(String monthExpense) {
        this.monthExpense = monthExpense;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getMonthDate() {
        return monthDate;
    }

    public void setMonthDate(Date monthDate) {
        this.monthDate = monthDate;
    }

    @Override
    public String toString() {
        return "MonthModel{" +
                "name='" + name + '\'' +
                ", monthNumber=" + monthNumber +
                ", monthExpense='" + monthExpense + '\'' +
                '}';
    }
}
