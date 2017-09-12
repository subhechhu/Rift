package com.np.rift.main.groupFragment;

/**
 * Created by subhechhu on 9/6/2017.
 */

public class GroupModel {
    String groupId, groupName, memberContribution, groupMembers, membersId, groupExpense;
    float expenses;
    boolean isSettled;

    public String getGroupExpense() {
        return groupExpense;
    }

    public void setGroupExpense(String groupExpense) {
        this.groupExpense = groupExpense;
    }

    public String getGroupMembers() {
        return groupMembers;
    }

    public void setGroupMembers(String groupMembers) {
        this.groupMembers = groupMembers;
    }

    public float getExpenses() {
        return expenses;
    }

    public void setExpenses(float expenses) {
        this.expenses = expenses;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getMemberContribution() {
        return memberContribution;
    }

    public void setMemberContribution(String memberContribution) {
        this.memberContribution = memberContribution;
    }

    public String getMembersId() {
        return membersId;
    }

    public void setMembersId(String membersId) {
        this.membersId = membersId;
    }

    public boolean isSettled() {
        return isSettled;
    }

    public void setSettled(boolean settled) {
        isSettled = settled;
    }

    @Override
    public String toString() {
        return "GroupModel{" +
                "groupId='" + groupId + '\'' +
                ", groupName='" + groupName + '\'' +
                ", memberContribution='" + memberContribution + '\'' +
                ", groupMembers='" + groupMembers + '\'' +
                ", membersId='" + membersId + '\'' +
                ", groupExpense='" + groupExpense + '\'' +
                ", expenses=" + expenses +
                ", isSettled=" + isSettled +
                '}';
    }
}
