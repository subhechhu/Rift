package com.np.rift.main.groupFragment;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.np.rift.R;

import java.util.List;


class CustomAdapterListGroup extends RecyclerView.Adapter<CustomAdapterListGroup.MyViewHolder> {
    private final List<GroupModel> groupList;
    private final Context context;
    private String TAG = getClass().getSimpleName();

    CustomAdapterListGroup(Context context, List<GroupModel> groupList) {
//        HashSet<String> checkSet = new HashSet<>();
        this.groupList = groupList;
        this.context = context;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.view_group, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {

        final GroupModel details = groupList.get(position);
        holder.textView_groupID.setText("#" + details.getGroupId());

        holder.textView_userExpense.setText("Contribution \n " + details.getMemberContribution());
        holder.textView_groupName.setText(details.getGroupName());

        if(details.isSettled){
            holder.view_divider.setBackgroundColor(ContextCompat.getColor(context,R.color.colorPrimaryDark));
        }else {
            holder.view_divider.setBackgroundColor(ContextCompat.getColor(context,R.color.dark_red));
        }

        holder.linearlayout_child.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent;
//                intent = new Intent(context, SettledActivity.class);
                if (details.isSettled) {
                    intent = new Intent(context, SettledActivity.class);
                } else {
                    intent = new Intent(context, GroupPieActivity.class);
                }
                intent.putExtra("group_name", details.getGroupName());
                intent.putExtra("group_id", details.getGroupId());
                intent.putExtra("group_expense", details.getGroupExpense());
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
        final TextView textView_userExpense;
        final TextView textView_groupName;
        final LinearLayout linearlayout_child;
        View view_divider;

        MyViewHolder(View view) {
            super(view);

            this.setIsRecyclable(false);

            textView_groupID = view.findViewById(R.id.textView_group);
            textView_userExpense = itemView.findViewById(R.id.textView_groupMembers);
            textView_groupName = itemView.findViewById(R.id.textView_groupName);
            linearlayout_child = itemView.findViewById(R.id.linearlayout_child);

            view_divider = itemView.findViewById(R.id.view_divider);
        }
    }
}