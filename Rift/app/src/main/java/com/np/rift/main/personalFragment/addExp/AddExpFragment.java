package com.np.rift.main.personalFragment.addExp;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.np.rift.AppController;
import com.np.rift.R;
import com.np.rift.connection.NetworkCheck;
import com.np.rift.main.groupFragment.GroupExpenseActivity;
import com.np.rift.main.groupFragment.GroupPieActivity;
import com.np.rift.util.SharedPrefUtil;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by subhechhu on 9/5/2017.
 */

public class AddExpFragment extends BottomSheetDialogFragment {

    private final SimpleDateFormat format_toSend = new SimpleDateFormat("MM-dd-yyyy");
    private final SimpleDateFormat format_toshow = new SimpleDateFormat("MMM dd, yyyy");
    EditText editText_productName, editText_productAmount;
    TextView textView_date;
    Button proceed;
    Calendar calendar = Calendar.getInstance(), calendar_current;
    DatePickerDialog datePickerDialog;
    JSONArray itemsArray = new JSONArray();
    View contentView;
    boolean isSent = false;

    String dateToSend, dateToShow;
    String forActivity;

    @Override
    public void setupDialog(final Dialog dialog, int style) {
        super.setupDialog(dialog, style);
        contentView = View.inflate(getContext(), R.layout.fragment_add_exp, null);
        dialog.setContentView(contentView);

        forActivity = getArguments().getString("for");

        editText_productName = contentView.findViewById(R.id.editText_productName);
        editText_productAmount = contentView.findViewById(R.id.editText_productAmount);
        textView_date = contentView.findViewById(R.id.textView_date);
        proceed = contentView.findViewById(R.id.button_proceed);

        dateToShow = format_toshow.format(calendar.getTime());
        dateToSend = format_toSend.format(calendar.getTime());

        textView_date.setText(dateToShow);

        textView_date.setOnClickListener(new View.OnClickListener() {
            int mYear, mMonth, mDay;

            @Override
            public void onClick(View view) {
                mYear = calendar.get(Calendar.YEAR);
                mMonth = calendar.get(Calendar.MONTH);
                mDay = calendar.get(Calendar.DAY_OF_MONTH);

                datePickerDialog = new DatePickerDialog(getActivity(),
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year,
                                                  int monthOfYear, int dayOfMonth) {
                                calendar.set(year, monthOfYear, dayOfMonth);
                                calendar_current = Calendar.getInstance();
                                if (calendar_current.before(calendar)) {
                                    Toast.makeText(getActivity(), "Invalid Time Selected", Toast.LENGTH_SHORT).show();
                                } else {
                                    Date date = calendar.getTime();
                                    dateToSend = format_toSend.format(date);
                                    dateToShow = format_toshow.format(date);

                                    textView_date.setText(dateToShow);
                                }
                            }
                        }, mYear, mMonth, mDay);
                datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
                datePickerDialog.show();
            }
        });

        proceed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (NetworkCheck.isInternetAvailable()) {
                    if (editText_productAmount.getText().toString().isEmpty() ||
                            editText_productName.getText().toString().isEmpty()) {
                        Toast.makeText(getActivity(), "Fields cannot be empty", Toast.LENGTH_SHORT).show();
                    } else {
                        try {
                            JSONObject itemObject = new JSONObject();
                            itemObject.put("date", dateToSend);
                            itemObject.put("spentOn", editText_productName.getText().toString().toLowerCase());
                            itemObject.put("amount", editText_productAmount.getText().toString());
                            itemsArray.put(itemObject);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        createDialog(dialog);
                    }
                } else {
                    if ("group".equalsIgnoreCase(forActivity)) {
                        ((GroupExpenseActivity) getActivity()).noInternet();
                    } else {
                        ((PersonalExpenseActivity) getActivity()).noInternet();
                    }
                }

            }
        });
    }

    @Override
    public void onStop() {
        super.onStop();
        if (itemsArray.length() > 0 && !isSent) {
            if ("group".equalsIgnoreCase(forActivity)) {
                isSent = true;
                ((GroupPieActivity) getActivity()).AddItems(itemsArray);
            } else {
                isSent = true;
                ((PersonalExpenseActivity) getActivity()).AddItems(itemsArray);
            }
        }
    }


    private void createDialog(final Dialog bottomDialog) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Confirmation")
                .setMessage("Do you want to add arrow_more items?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        editText_productName.setText("");
                        editText_productAmount.setText("");
                        dialog.dismiss();
                    }
                }).setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                editText_productName.setText("");
                editText_productAmount.setText("");
                dialog.dismiss();
                if ("group".equalsIgnoreCase(forActivity)) {
                    isSent = true;
                    ((GroupPieActivity) getActivity()).AddItems(itemsArray);
                } else {
                    isSent = true;
                    ((PersonalExpenseActivity) getActivity()).AddItems(itemsArray);
                }
                bottomDialog.dismiss();
            }
        })
                .show();
    }

    private void showSnackBar(String message) {
        final Snackbar _snackbar = Snackbar.make(contentView, message, Snackbar.LENGTH_LONG);
        _snackbar.setAction("OK", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                _snackbar.dismiss();
            }
        }).show();
    }

}