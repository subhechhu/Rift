package com.np.rift.main.groupFragment;

/**
 * Created by subhechhu on 9/6/2017.
 */

public class GroupModel {
    String groupId, groupName, memberContribution, groupMembers, membersId, groupExpense, settleId,
            settleBy, settledOn;
    float expenses;
    int memberCount;
    boolean isSettled;

    public String getSettleId() {
        return settleId;
    }

    public void setSettleId(String settleId) {
        this.settleId = settleId;
    }

    public String getSettleBy() {
        return settleBy;
    }

    public void setSettleBy(String settleBy) {
        this.settleBy = settleBy;
    }

    public String getSettledOn() {
        return settledOn;
    }

    public void setSettledOn(String settledOn) {
        this.settledOn = settledOn;
    }

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

    public int getMemberCount() {
        return memberCount;
    }

    public void setMemberCount(int memberCount) {
        this.memberCount = memberCount;
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
                ", settleId='" + settleId + '\'' +
                ", settleBy='" + settleBy + '\'' +
                ", settledOn='" + settledOn + '\'' +
                ", expenses=" + expenses +
                ", memberCount=" + memberCount +
                ", isSettled=" + isSettled +
                '}';
    }
}
