package com.np.rift.main.notificationFragment;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.np.rift.R;
import com.np.rift.main.groupFragment.GroupModel;

import java.util.HashSet;
import java.util.List;


class CustomAdapterNotification extends RecyclerView.Adapter<CustomAdapterNotification.MyViewHolder> {
    private final List<GroupModel> groupList;
    private final Context context;
    private String TAG = getClass().getSimpleName();

    CustomAdapterNotification(Context context, List<GroupModel> groupList) {
        HashSet<String> checkSet = new HashSet<>();
        this.groupList = groupList;
        this.context = context;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.view_notification, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {

        final GroupModel details = groupList.get(position);
        holder.textView_groupID.setText(details.getGroupName());

    }

    @Override
    public int getItemCount() {
        return groupList.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        final LinearLayout linearlayout_child;
        TextView textView_groupID;

        MyViewHolder(View view) {
            super(view);

            this.setIsRecyclable(false);

            textView_groupID = view.findViewById(R.id.textView_groupID);
            linearlayout_child = itemView.findViewById(R.id.linearlayout_child);

        }
    }
}