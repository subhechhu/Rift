package com.np.rift.main.groupFragment;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.np.rift.R;

import java.util.HashSet;
import java.util.List;


class CustomAdapterGroup extends RecyclerView.Adapter<CustomAdapterGroup.MyViewHolder> {
    private final HashSet<String> checkSet;
    private final List<GroupModel> groupList;
    private final Context context;
    private String TAG = getClass().getSimpleName();

    CustomAdapterGroup(Context context, List<GroupModel> groupList) {
        checkSet = new HashSet<>();
        this.groupList = groupList;
        this.context = context;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.group_view, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {

        final GroupModel details = groupList.get(position);
        holder.textView_groupID.setText("#"+details.getGroupId());
        if(Integer.parseInt(details.getGroupMembersCount())>1){
            holder.textView_groupMembers.setText(details.getGroupMembersCount()+" members");
        }else {
            holder.textView_groupMembers.setText(details.getGroupMembersCount()+" member");
        }
        holder.textView_groupName.setText(details.getGroupName());

        holder.linearlayout_child.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.e(TAG,"id: "+details.getGroupId());
                Log.e(TAG,"grp name: "+details.getGroupName());
                Intent intent=new Intent(context,GroupActivity.class);
                intent.putExtra("group_name",details.getGroupName());
                intent.putExtra("group_id",details.getGroupId());
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return groupList.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        final TextView textView_groupID;
        final TextView textView_groupMembers;
        final TextView textView_groupName;
        final LinearLayout linearlayout_child;

        MyViewHolder(View view) {
            super(view);

            this.setIsRecyclable(false);

            textView_groupID = view.findViewById(R.id.textView_groupID);
            textView_groupMembers = itemView.findViewById(R.id.textView_groupMembers);
            textView_groupName = itemView.findViewById(R.id.textView_groupName);
            linearlayout_child = itemView.findViewById(R.id.linearlayout_child);

        }
    }
}