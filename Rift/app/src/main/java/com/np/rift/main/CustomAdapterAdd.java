package com.np.rift.main;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.np.rift.R;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Created by subhechhu on 9/5/2017.
 */

class CustomAdapterAdd extends BaseExpandableListAdapter {
    private final HashSet<String> checkSetID;
    int resource;
    DeleteInfo mDeleteInfo;
    private Context context;
    private ArrayList<MonthModel> monthArray;
    private LinkedHashMap<String, List<ExpenseModel>> listExpenses;
    private LayoutInflater inflater;
    private int selectedCount = 0;
    private ViewHolder holder;
    private ExpenseModel expenseModel;


    CustomAdapterAdd(LayoutInflater inflater, Context context, ArrayList<MonthModel> monthArray,
                     LinkedHashMap<String, List<ExpenseModel>> listExpenses) {
        checkSetID = new HashSet<>();
        selectedCount = 0;
        this.context = context;
        this.monthArray = monthArray;
        this.listExpenses = listExpenses;
        this.inflater = inflater;
    }

    @Override
    public int getGroupCount() {
        return (monthArray.size());
    }

    @Override
    public int getChildrenCount(int i) {
        String name = monthArray.get(i).getName();
        List<ExpenseModel> myList = listExpenses.get(name);
        return myList.size();
    }

    @Override
    public Object getGroup(int i) {
        return monthArray;
    }

    @Override
    public Object getChild(int i, int i1) {
        return listExpenses.get(monthArray.get(i).getName());
    }

    @Override
    public long getGroupId(int i) {
        return i;
    }

    @Override
    public long getChildId(int i, int i1) {
        return i * 1024 + i1;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public View getGroupView(int i, boolean b, View view, ViewGroup viewGroup) {

        holder = new ViewHolder();
        if (view == null) {
            view = inflater.inflate(R.layout.month_view, viewGroup, false);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }
        MonthModel rowItem = monthArray.get(i);
        holder.title = view.findViewById(R.id.textView_title);
        holder.title.setText(rowItem.getName());
        return (view);
    }

    @Override
    public View getChildView(int i, int i1, boolean b, View view, ViewGroup viewGroup) {
        holder = new ViewHolder();
        if (view == null) {
            view = inflater.inflate(R.layout.expense_view, viewGroup, false);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }

        String name = monthArray.get(i).getName();
        List<ExpenseModel> myList = listExpenses.get(name);

        expenseModel = myList.get(i1);

        holder.nameString = expenseModel.getName();
        holder.descriptionString = expenseModel.getDescription();
        holder.priceString = expenseModel.getPrice();

        holder.linearlayout_child = view.findViewById(R.id.linearlayout_child);
        holder.name = view.findViewById(R.id.textView_name);
        holder.price = view.findViewById(R.id.textView_price);
        holder.description = view.findViewById(R.id.textView_description);
        holder.name.setText(holder.nameString);
        holder.price.setText("Rs." + holder.priceString);
        holder.description.setText(holder.descriptionString);

        return (view);
    }

    @Override
    public boolean isChildSelectable(int i, int i1) {
        return false;
    }

    interface DeleteInfo {
        void deleteDetails(int selectedCount, HashSet<String> selectedSet);
    }

    private static class ViewHolder {
        TextView title;
        TextView description;
        TextView price;
        TextView name;

        LinearLayout linearlayout_child;
        String nameString, priceString, descriptionString;
    }
}


