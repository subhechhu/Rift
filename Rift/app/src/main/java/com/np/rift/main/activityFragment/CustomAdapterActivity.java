package com.np.rift.main.activityFragment;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.np.rift.R;

import java.util.HashSet;
import java.util.List;


class CustomAdapterActivity extends RecyclerView.Adapter<CustomAdapterActivity.MyViewHolder> {
    private final List<ActivityModel> groupList;
    private final Context context;
    private String TAG = getClass().getSimpleName();

    CustomAdapterActivity(Context context, List<ActivityModel> groupList) {
        HashSet<String> checkSet = new HashSet<>();
        this.groupList = groupList;
        this.context = context;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Log.e("TAG", "viewType: " + viewType);
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.view_notification, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {

        final ActivityModel details = groupList.get(position);
        holder.textView_group.setText("#" + details.getGroupName());

//        holder.textView_date.setText("Contribution \n " + details.getMemberContribution());
//        holder.textView_groupName.setText(details.getGroupName());

//        if(details.isSettled){
//            holder.view_divider.setBackgroundColor(ContextCompat.getColor(context,R.color.colorPrimaryDark));
//        }else {
//            holder.view_divider.setBackgroundColor(ContextCompat.getColor(context,R.color.dark_red));
//        }

//        holder.linearlayout_child.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
////                Intent intent;
//////                intent = new Intent(context, SettledActivity.class);
////                if (details.isSettled) {
////                    intent = new Intent(context, SettledActivity.class);
////                } else {
////                    intent = new Intent(context, GroupPieActivity.class);
////                }
////                intent.putExtra("group_name", details.getGroupName());
////                intent.putExtra("group_id", details.getGroupId());
////                intent.putExtra("group_expense", details.getGroupExpense());
////                context.startActivity(intent);
//            }
//        });
    }

    @Override
    public int getItemCount() {
        return groupList.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        final TextView textView_group;
        final TextView textView_date;
        View view_divider;

        MyViewHolder(View view) {
            super(view);
            this.setIsRecyclable(false);
            textView_group = view.findViewById(R.id.textView_group);
            textView_date = itemView.findViewById(R.id.textView_date);
            view_divider = itemView.findViewById(R.id.view_divider);
        }
    }
}