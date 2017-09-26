package com.np.rift.main.menuOptions;

import android.app.Dialog;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.support.design.widget.BottomSheetDialogFragment;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.np.rift.R;

/**
 * Created by subhechhu on 9/5/2017.
 */

public class AboutFragment extends BottomSheetDialogFragment {
    TextView textView_version;
    Button button_close;
    View contentView;

    @Override
    public void setupDialog(final Dialog dialog, int style) {
        super.setupDialog(dialog, style);
        contentView = View.inflate(getContext(), R.layout.fragment_about, null);
        dialog.setContentView(contentView);
        button_close = contentView.findViewById(R.id.button_close);
        textView_version = contentView.findViewById(R.id.textView_version);

        try {
            PackageManager manager = getActivity().getPackageManager();
            PackageInfo info = manager.getPackageInfo(
                    getActivity().getPackageName(), 0);
            textView_version.setText(info.versionName);
        } catch (Exception e) {
            e.printStackTrace();
        }

        button_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });


    }
}