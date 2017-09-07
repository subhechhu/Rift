package com.np.rift.main.groupFragment;

import org.json.JSONArray;

import java.util.List;

/**
 * Created by subhechhu on 9/6/2017.
 */

public class GroupModel {
    String groupId, groupName, groupMembersCount, groupMembers, membersId;
    float expenses;

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

    public String getGroupMembersCount() {
        return groupMembersCount;
    }

    public void setGroupMembersCount(String groupMembersCount) {
        this.groupMembersCount = groupMembersCount;
    }

    public String getMembersId() {
        return membersId;
    }

    public void setMembersId(String membersId) {
        this.membersId = membersId;
    }

    @Override
    public String toString() {
        return "GroupModel{" +
                "groupId='" + groupId + '\'' +
                ", groupName='" + groupName + '\'' +
                ", groupMembersCount='" + groupMembersCount + '\'' +
                ", groupMembers='" + groupMembers + '\'' +
                ", expenses=" + expenses +
                '}';
    }
}
